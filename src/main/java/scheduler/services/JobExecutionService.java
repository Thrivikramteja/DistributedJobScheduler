package scheduler.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scheduler.config.SchedulerMetrics;
import scheduler.domain.Job;
import scheduler.store.JobRepository;
import scheduler.worker.TaskHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);
    private final JobRepository jobRepository;
    private final SchedulerMetrics metrics;
    private Map<String, TaskHandler> handlerMap = new HashMap<>();

    public JobExecutionService(JobRepository jobRepository, List<TaskHandler> handlers, SchedulerMetrics metrics) {
        this.jobRepository = jobRepository;
        this.metrics = metrics;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(TaskHandler::getType, h -> h));
    }

    @Transactional
    public void execute(Job job) {
        String type = job.getType();
        JobHandler handler = handlerMap.get(type);
        String payload = job.getPayload();
        if(handler == null) {
            log.error("Unknown job type: " + type);
            job.setStatus(Job.JobStatus.FAILED);
            job.setFinishedAt(Instant.now());
            jobRepository.saveAndFlush(job);
            metrics.jobFailed(type);
            return;
        }
        try {
            handler.execute(job);
            job.setStatus((Job.JobStatus.COMPLETED));
            job.setFinishedAt(Instant.now());
            jobRepository.saveAndFlush(job);
            metrics.jobCompleted(type);
        } catch (Exception e) {
            job.setRetryCount(job.getRetryCount() + 1);
            job.setLastError(e.getMessage());
            if(job.getRetryCount() >= job.getMaxRetries()) {
                job.setStatus(Job.JobStatus.FAILED);
                job.setFinishedAt(Instant.now());
                jobRepository.saveAndFlush(job);
                metrics.jobFailed(type);
            } else {
                job.setStatus(Job.JobStatus.PENDING);
                job.setWorkerId(null);
                job.setStartedAt(null);
                job.setClaimedAt(null);
                jobRepository.saveAndFlush(job);
                metrics.jobRetry(type);
            }
        }
    }
}
