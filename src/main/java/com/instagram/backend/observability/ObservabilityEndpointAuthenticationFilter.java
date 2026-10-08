package com.instagram.backend.observability;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 110)
public class ObservabilityEndpointAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "X-Observability-Token";

    private final String configuredToken;
    private final String actuatorBasePath;

    public ObservabilityEndpointAuthenticationFilter(
            @Value("${app.observability.metrics-token:}") String configuredToken,
            @Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath) {
        this.configuredToken = configuredToken;
        this.actuatorBasePath = normalizeBasePath(actuatorBasePath);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathWithinApplication(request);
        String healthPath = actuatorBasePath + "/health";
        boolean actuatorRequest = path.equals(actuatorBasePath) || path.startsWith(actuatorBasePath + "/");
        boolean publicHealthRequest = path.equals(healthPath) || path.startsWith(healthPath + "/");
        return !actuatorRequest || publicHealthRequest;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (configuredToken.isBlank()) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "observability_token_not_configured");
            return;
        }

        String suppliedToken = request.getHeader(TOKEN_HEADER);
        if (!constantTimeEquals(configuredToken, suppliedToken)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_observability_token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isEmpty()) {
            return servletPath;
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private String normalizeBasePath(String basePath) {
        String normalized = basePath == null ? "" : basePath.trim();
        if (normalized.isEmpty() || "/".equals(normalized)) {
            throw new IllegalArgumentException(
                    "management.endpoints.web.base-path must be a non-root path so diagnostics can be isolated");
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private void writeError(HttpServletResponse response, int status, String errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + errorCode + "\"}");
    }
}
