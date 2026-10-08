package com.instagram.backend.observability;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class InstagramMetrics {

    private static final String BUSINESS_OPERATIONS = "instagram.business.operations";
    private static final String MEDIA_UPLOAD_SIZE = "instagram.media.upload.size";

    private final Map<JourneyOutcome, Counter> journeyCounters = new HashMap<>();
    private final Map<MediaKind, DistributionSummary> mediaUploadSizes = new EnumMap<>(MediaKind.class);

    public InstagramMetrics(MeterRegistry meterRegistry) {
        for (Journey journey : Journey.values()) {
            for (Outcome outcome : Outcome.values()) {
                JourneyOutcome key = new JourneyOutcome(journey, outcome);
                journeyCounters.put(key, Counter.builder(BUSINESS_OPERATIONS)
                        .description("Completed user-facing operations by bounded journey and outcome")
                        .tag("operation", journey.tagValue)
                        .tag("outcome", outcome.tagValue)
                        .register(meterRegistry));
            }
        }

        for (MediaKind mediaKind : MediaKind.values()) {
            mediaUploadSizes.put(mediaKind, DistributionSummary.builder(MEDIA_UPLOAD_SIZE)
                    .description("Successfully stored media payload size")
                    .baseUnit("bytes")
                    .tag("media.type", mediaKind.tagValue)
                    .register(meterRegistry));
        }
    }

    public void recordJourney(Journey journey, Outcome outcome) {
        journeyCounters.get(new JourneyOutcome(journey, outcome)).increment();
    }

    public void recordMediaUpload(long sizeInBytes, String contentType) {
        mediaUploadSizes.get(MediaKind.from(contentType)).record(sizeInBytes);
    }

    public enum Journey {
        REGISTRATION("registration"),
        LOGIN("login"),
        FEED("feed"),
        POST_CREATE("post_create"),
        MEDIA_UPLOAD("media_upload");

        private final String tagValue;

        Journey(String tagValue) {
            this.tagValue = tagValue;
        }
    }

    public enum Outcome {
        SUCCESS("success"),
        FAILURE("failure");

        private final String tagValue;

        Outcome(String tagValue) {
            this.tagValue = tagValue;
        }
    }

    private enum MediaKind {
        IMAGE("image"),
        VIDEO("video"),
        OTHER("other");

        private final String tagValue;

        MediaKind(String tagValue) {
            this.tagValue = tagValue;
        }

        private static MediaKind from(String contentType) {
            String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
            if (normalized.startsWith("image/")) {
                return IMAGE;
            }
            if (normalized.startsWith("video/")) {
                return VIDEO;
            }
            return OTHER;
        }
    }

    private record JourneyOutcome(Journey journey, Outcome outcome) {
    }
}
