package com.instagram.backend.observability;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ObservabilityManagementPortGuardTests {

    @Test
    void samePortDefaultIsAllowed() {
        assertThatCode(() -> new ObservabilityManagementPortGuard("", "8080"))
                .doesNotThrowAnyException();
        assertThatCode(() -> new ObservabilityManagementPortGuard("8080", "8080"))
                .doesNotThrowAnyException();
    }

    @Test
    void separateManagementPortFailsClosedUntilItsContextHasAuthentication() {
        assertThatThrownBy(() -> new ObservabilityManagementPortGuard("9083", "8083"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("separate management.server.port is unsupported");
    }
}
