package com.instagram.backend.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ObservabilityManagementPortGuard {

    public ObservabilityManagementPortGuard(
            @Value("${management.server.port:}") String managementServerPort,
            @Value("${server.port:8080}") String applicationServerPort) {
        if (StringUtils.hasText(managementServerPort)
                && !managementServerPort.trim().equals(applicationServerPort.trim())) {
            throw new IllegalStateException(
                    "A separate management.server.port is unsupported because the observability token filter "
                            + "runs in the main web context. Keep Actuator on the main port or add security "
                            + "inside the management context before enabling a separate port.");
        }
    }
}
