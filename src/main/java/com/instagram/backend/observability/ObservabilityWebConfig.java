package com.instagram.backend.observability;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ObservabilityWebConfig implements WebMvcConfigurer {

    private final JourneyMetricsInterceptor journeyMetricsInterceptor;

    public ObservabilityWebConfig(JourneyMetricsInterceptor journeyMetricsInterceptor) {
        this.journeyMetricsInterceptor = journeyMetricsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(journeyMetricsInterceptor);
    }
}
