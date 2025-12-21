package scheduler.api.dto;

import java.util.UUID;

public record CreateJobResponse(UUID jobId, String status) {
}
