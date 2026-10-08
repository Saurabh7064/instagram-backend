package com.instagram.backend.observability;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.instagram.backend.observability.InstagramMetrics.Journey;
import com.instagram.backend.observability.InstagramMetrics.Outcome;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JourneyMetricsInterceptor implements HandlerInterceptor {

    private static final Map<RouteKey, Journey> JOURNEYS = Map.of(
            new RouteKey("POST", "/api/auth/register"), Journey.REGISTRATION,
            new RouteKey("POST", "/api/auth/login"), Journey.LOGIN,
            new RouteKey("GET", "/api/feed"), Journey.FEED,
            new RouteKey("POST", "/api/posts"), Journey.POST_CREATE,
            new RouteKey("POST", "/api/media"), Journey.MEDIA_UPLOAD);

    private final InstagramMetrics instagramMetrics;

    public JourneyMetricsInterceptor(InstagramMetrics instagramMetrics) {
        this.instagramMetrics = instagramMetrics;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        Object bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchingPattern == null) {
            return;
        }

        Journey journey = JOURNEYS.get(new RouteKey(request.getMethod(), bestMatchingPattern.toString()));
        if (journey == null) {
            return;
        }

        Outcome outcome = exception == null && response.getStatus() < 400 ? Outcome.SUCCESS : Outcome.FAILURE;
        instagramMetrics.recordJourney(journey, outcome);
    }

    private record RouteKey(String method, String route) {
    }
}
