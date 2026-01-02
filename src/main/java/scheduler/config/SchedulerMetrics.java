package scheduler.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Central registry for dispatch-engine metrics.
 * All counters share the {@value #EVENTS_METRIC} base name and are differentiated
 * by {@value #TAG_EVENT} (lifecycle event) and {@value #TAG_TYPE} (task type) tags.
 */
@Component
public final class SchedulerMetrics {

    public static final String EVENTS_METRIC = "task.dispatch.events";
    public static final String TAG_EVENT = "event";
    public static final String TAG_TYPE = "type";

    private final MeterRegistry registry;

    public SchedulerMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void jobCreated(String jobType) {
        counter("created", jobType).increment();
    }

    public void jobCompleted(String jobType) {
        counter("completed", jobType).increment();
    }

    public void jobFailed(String jobType) {
        counter("failed", jobType).increment();
    }

    public void jobCancelled(String jobType) {
        counter("cancelled", jobType).increment();
    }

    public void jobClaimed(String jobType) {
        counter("claimed", jobType).increment();
    }

    public void jobRetry(String jobType) {
        counter("retry", jobType).increment();
    }

    public void jobStaleReleased() {
        Counter.builder(EVENTS_METRIC)
                .tag(TAG_EVENT, "stale_released")
                .register(registry)
                .increment();
    }

    private Counter counter(String event, String jobType) {
        return Counter.builder(EVENTS_METRIC)
                .tag(TAG_EVENT, event)
                .tag(TAG_TYPE, jobType != null ? jobType : "unknown")
                .register(registry);
    }
}
