package com.instagram.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.logging.logback.StructuredLogEncoder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.instagram.backend.observability.ObservabilityEndpointAuthenticationFilter;
import com.instagram.backend.observability.RequestCorrelationFilter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest(properties = {
        "app.observability.metrics-token=integration-observability-token",
        "management.tracing.sampling.probability=1.0",
        "management.tracing.export.otlp.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
class ObservabilityIntegrationTests {

    private static final String OBSERVABILITY_TOKEN = "integration-observability-token";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("instagram")
            .withUsername("instagram")
            .withPassword("instagram");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-at-least-32-bytes");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void healthAndProbeEndpointsArePublicWithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/livez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/readyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void readinessReportsOutOfServiceWhenApplicationRefusesTraffic() throws Exception {
        try {
            AvailabilityChangeEvent.publish(applicationContext, ReadinessState.REFUSING_TRAFFIC);

            mockMvc.perform(get("/actuator/health/readiness"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"));
        } finally {
            AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC);
        }
    }

    @Test
    void diagnosticEndpointsRequireTheDedicatedObservabilityToken() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/prometheus")
                        .header(ObservabilityEndpointAuthenticationFilter.TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/prometheus")
                        .header(ObservabilityEndpointAuthenticationFilter.TOKEN_HEADER, OBSERVABILITY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("http_server_requests")))
                .andExpect(content().string(containsString("instagram_business_operations_total")));

        mockMvc.perform(get("/actuator/env")
                        .header(ObservabilityEndpointAuthenticationFilter.TOKEN_HEADER, OBSERVABILITY_TOKEN))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/me")
                        .header(ObservabilityEndpointAuthenticationFilter.TOKEN_HEADER, OBSERVABILITY_TOKEN))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/instagram/actuator/prometheus")
                        .contextPath("/instagram"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/instagram/actuator/prometheus")
                        .contextPath("/instagram")
                        .header(ObservabilityEndpointAuthenticationFilter.TOKEN_HEADER, OBSERVABILITY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("http_server_requests")));
    }

    @Test
    void loginIncrementsTheBoundedBusinessOutcomeMetric() throws Exception {
        double before = meterRegistry.get("instagram.business.operations")
                .tag("operation", "login")
                .tag("outcome", "success")
                .counter()
                .count();

        loginAndExtractAccessToken();

        double after = meterRegistry.get("instagram.business.operations")
                .tag("operation", "login")
                .tag("outcome", "success")
                .counter()
                .count();
        assertThat(after).isEqualTo(before + 1.0);
    }

    @Test
    void feedCreatesALocalObservationAndIncrementsItsBusinessMetric() throws Exception {
        String accessToken = loginAndExtractAccessToken();
        double observationCountBefore = feedObservationCount();
        double successCountBefore = meterRegistry.get("instagram.business.operations")
                .tag("operation", "feed")
                .tag("outcome", "success")
                .counter()
                .count();

        mockMvc.perform(get("/api/feed")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        double observationCountAfter = feedObservationCount();
        double successCountAfter = meterRegistry.get("instagram.business.operations")
                .tag("operation", "feed")
                .tag("outcome", "success")
                .counter()
                .count();
        assertThat(observationCountAfter).isEqualTo(observationCountBefore + 1.0);
        assertThat(successCountAfter).isEqualTo(successCountBefore + 1.0);
    }

    @Test
    void successfulMediaUploadRecordsOnlyABoundedMediaCategoryAndOutcome() throws Exception {
        String accessToken = loginAndExtractAccessToken();
        byte[] imageBytes = "small-observability-image".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "debug-photo.png",
                MediaType.IMAGE_PNG_VALUE,
                imageBytes);
        long sizeCountBefore = meterRegistry.get("instagram.media.upload.size")
                .tag("media.type", "image")
                .summary()
                .count();
        double sizeTotalBefore = meterRegistry.get("instagram.media.upload.size")
                .tag("media.type", "image")
                .summary()
                .totalAmount();
        double successCountBefore = meterRegistry.get("instagram.business.operations")
                .tag("operation", "media_upload")
                .tag("outcome", "success")
                .counter()
                .count();

        mockMvc.perform(multipart("/api/media")
                        .file(image)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());

        var sizeSummary = meterRegistry.get("instagram.media.upload.size")
                .tag("media.type", "image")
                .summary();
        double successCountAfter = meterRegistry.get("instagram.business.operations")
                .tag("operation", "media_upload")
                .tag("outcome", "success")
                .counter()
                .count();
        assertThat(sizeSummary.count()).isEqualTo(sizeCountBefore + 1);
        assertThat(sizeSummary.totalAmount()).isEqualTo(sizeTotalBefore + imageBytes.length);
        assertThat(successCountAfter).isEqualTo(successCountBefore + 1.0);
    }

    @Test
    void correlationIdIsPreservedGeneratedAndCleanedUp() throws Exception {
        String suppliedCorrelationId = "integration-request-123";

        mockMvc.perform(get("/actuator/health")
                        .header(RequestCorrelationFilter.CORRELATION_HEADER, suppliedCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestCorrelationFilter.CORRELATION_HEADER, suppliedCorrelationId));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        RequestCorrelationFilter.CORRELATION_HEADER,
                        matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));

        mockMvc.perform(get("/actuator/health")
                        .header(RequestCorrelationFilter.CORRELATION_HEADER, "unsafe correlation id with spaces"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        RequestCorrelationFilter.CORRELATION_HEADER,
                        matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));

        assertThat(MDC.get(RequestCorrelationFilter.MDC_KEY)).isNull();
    }

    @Test
    void structuredCompletionLogUsesNormalizedRouteAndOmitsSecrets() throws Exception {
        String accessToken = loginAndExtractAccessToken();
        String correlationId = "normalized-route-check";
        String secretSentinel = "must-not-appear-in-request-log";
        ListAppender<ILoggingEvent> appender = attachRequestLogAppender();

        try {
            mockMvc.perform(post("/api/posts/987654321/like")
                            .header("Authorization", "Bearer " + accessToken)
                            .header("X-Debug-Secret", secretSentinel)
                            .header(RequestCorrelationFilter.CORRELATION_HEADER, correlationId))
                    .andExpect(status().isNotFound());

            ILoggingEvent completion = onlyCompletionEvent(appender);
            Map<String, Object> fields = eventFields(completion);

            assertThat(completion.getFormattedMessage()).isEqualTo("HTTP request completed");
            assertThat(fields)
                    .containsEntry("event", "http.request.completed")
                    .containsEntry("method", "POST")
                    .containsEntry("route", "/api/posts/{postId}/like")
                    .containsEntry("status", 404);
            assertThat(fields.toString())
                    .doesNotContain("/api/posts/987654321/like")
                    .doesNotContain(secretSentinel)
                    .doesNotContain(accessToken);
            assertThat(completion.getMDCPropertyMap()).containsEntry(RequestCorrelationFilter.MDC_KEY, correlationId);

            String encodedEvent = encodeAsEcsJson(completion);
            assertThat(encodedEvent)
                    .contains("\"event\":\"http.request.completed\"")
                    .contains("\"correlationId\":\"" + correlationId + "\"")
                    .doesNotContain(secretSentinel)
                    .doesNotContain(accessToken);
        } finally {
            detachRequestLogAppender(appender);
        }
    }

    @Test
    void incomingW3cTraceContextAppearsInTheRequestLog() throws Exception {
        String traceId = "0af7651916cd43dd8448eb211c80319c";
        String traceParent = "00-" + traceId + "-b7ad6b7169203331-01";
        ListAppender<ILoggingEvent> appender = attachRequestLogAppender();

        try {
            mockMvc.perform(get("/actuator/health")
                            .header("traceparent", traceParent)
                            .header(RequestCorrelationFilter.CORRELATION_HEADER, "trace-log-check"))
                    .andExpect(status().isOk());

            ILoggingEvent completion = onlyCompletionEvent(appender);
            assertThat(completion.getMDCPropertyMap()).containsEntry("traceId", traceId);
        } finally {
            detachRequestLogAppender(appender);
        }
    }

    private String loginAndExtractAccessToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier":"a",
                                  "password":"a"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String response = login.getResponse().getContentAsString();
        String marker = "\"accessToken\":\"";
        int start = response.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("accessToken not found in login response");
        }
        int tokenStart = start + marker.length();
        int tokenEnd = response.indexOf('"', tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }

    private ListAppender<ILoggingEvent> attachRequestLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestCorrelationFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachRequestLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestCorrelationFilter.class);
        logger.detachAppender(appender);
        appender.stop();
    }

    private ILoggingEvent onlyCompletionEvent(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream()
                .filter(event -> "HTTP request completed".equals(event.getFormattedMessage()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("Request completion log was not emitted"));
    }

    private Map<String, Object> eventFields(ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
                .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
    }

    private double feedObservationCount() {
        var timer = meterRegistry.find("instagram.feed.load")
                .tag("operation", "feed")
                .timer();
        return timer == null ? 0.0 : timer.count();
    }

    private String encodeAsEcsJson(ILoggingEvent event) {
        StructuredLogEncoder encoder = new StructuredLogEncoder();
        encoder.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        encoder.setFormat("ecs");
        encoder.start();
        try {
            return new String(encoder.encode(event), StandardCharsets.UTF_8);
        } finally {
            encoder.stop();
        }
    }
}
