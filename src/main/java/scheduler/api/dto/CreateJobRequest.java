package scheduler.api.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;

public class CreateJobRequest {

    @NotBlank(message = "type is required")
    @Size(max = 128)
    private String type;

    private String payload;

    @FutureOrPresent
    private Instant scheduledAt;

    @Min(0)
    @Max(100)
    private Integer maxRetries = 3;

    /** Task urgency — higher values are dispatched before lower ones. Defaults to 0. */
    @Min(0)
    @Max(100)
    private int priority = 0;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt (Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
