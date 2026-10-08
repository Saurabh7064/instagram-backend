package com.instagram.backend.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ObservabilityEndpointAuthenticationFilterTests {

    @Test
    void configurableActuatorBasePathRemainsProtectedBehindAContextPath() throws Exception {
        ObservabilityEndpointAuthenticationFilter filter =
                new ObservabilityEndpointAuthenticationFilter("operations-secret", "/operations/");
        MockHttpServletRequest request = requestInsideContextPath("/operations/prometheus");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> chainCalled.set(true));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chainCalled).isFalse();
    }

    @Test
    void matchingTokenAllowsConfigurableActuatorBasePath() throws Exception {
        ObservabilityEndpointAuthenticationFilter filter =
                new ObservabilityEndpointAuthenticationFilter("operations-secret", "/operations");
        MockHttpServletRequest request = requestInsideContextPath("/operations/prometheus");
        request.addHeader(ObservabilityEndpointAuthenticationFilter.TOKEN_HEADER, "operations-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> chainCalled.set(true));

        assertThat(chainCalled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void healthUnderConfigurableBasePathStaysPublic() throws Exception {
        ObservabilityEndpointAuthenticationFilter filter =
                new ObservabilityEndpointAuthenticationFilter("operations-secret", "/operations");
        MockHttpServletRequest request = requestInsideContextPath("/operations/health/readiness");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> chainCalled.set(true));

        assertThat(chainCalled).isTrue();
    }

    @Test
    void rootActuatorBasePathIsRejectedInsteadOfLeavingDiagnosticsAmbiguous() {
        assertThatThrownBy(() -> new ObservabilityEndpointAuthenticationFilter("operations-secret", "/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-root path");
    }

    private MockHttpServletRequest requestInsideContextPath(String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/instagram" + servletPath);
        request.setContextPath("/instagram");
        request.setServletPath(servletPath);
        return request;
    }
}
