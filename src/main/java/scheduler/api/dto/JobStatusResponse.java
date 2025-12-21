package scheduler.api.dto;

import java.time.Instant;
import java.util.UUID;

public record JobStatusResponse(
        UUID jobId,
        String type,
        String status,
        String payload,
        int priority,
        Instant createdAt,
        Instant scheduledAt,
        Instant startedAt,
        Instant finishedAt,
        Integer retryCount,
        Integer maxRetries,
        String lastError
) {}
