package scheduler.worker;

import scheduler.domain.Job;

/**
 * Contract for pluggable task handlers.
 *
 * <p>Implement this interface and annotate the class with {@code @Component} to make
 * it available to the execution engine. The {@link #getType()} return value must match
 * the {@code type} field submitted in the create-task request.
 */
public interface TaskHandler {
    /** Returns the task type string this handler is responsible for. */
    String getType();

    /**
     * Executes the task. Throw any {@link RuntimeException} to signal a transient failure;
     * the engine will retry up to {@code maxRetries} times before marking the task FAILED.
     */
    void execute(Job job);
}
