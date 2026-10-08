package com.instagram.backend.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class JourneyMetricsInterceptorTests {

    @Test
    void unhandledExceptionIsFailureEvenBeforeTheResponseStatusChangesTo500() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        InstagramMetrics instagramMetrics = new InstagramMetrics(meterRegistry);
        JourneyMetricsInterceptor interceptor = new JourneyMetricsInterceptor(instagramMetrics);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/feed");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/feed");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.afterCompletion(request, response, new Object(), new IllegalStateException("feed failed"));

        assertThat(meterRegistry.get("instagram.business.operations")
                .tag("operation", "feed")
                .tag("outcome", "failure")
                .counter()
                .count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("instagram.business.operations")
                .tag("operation", "feed")
                .tag("outcome", "success")
                .counter()
                .count()).isZero();
    }
}
