package scheduler.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import scheduler.api.dto.CreateJobRequest;
import scheduler.config.SchedulerMetrics;
import scheduler.domain.Job;
import scheduler.store.JobRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final SchedulerMetrics metrics;

    public JobService(JobRepository jobRepository, SchedulerMetrics metrics) {
        this.jobRepository = jobRepository;
        this.metrics = metrics;
    }

    public Job create(CreateJobRequest request) {
        Job job = new Job();
        job.setExternalId(UUID.randomUUID());
        job.setType(request.getType());
        job.setPayload(request.getPayload());
        job.setStatus(Job.JobStatus.PENDING);
        job.setCreatedAt(Instant.now());
        job.setScheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : Instant.now());
        job.setStartedAt(null);
        job.setFinishedAt(null);
        job.setWorkerId(null);
        job.setClaimedAt(null);
        job.setRetryCount(0);
        job.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3);
        job.setLastError(null);
        job.setPriority(request.getPriority());
        jobRepository.save(job);
        metrics.jobCreated(job.getType());
        return job;
    }

    public Optional<Job> getById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jobRepository.findByExternalId(uuid);
        } catch (IllegalArgumentException e) {
            try {
                long longId = Long.parseLong(id);
                return jobRepository.findById(longId);
            } catch (NumberFormatException e2) {
                return Optional.empty();
            }
        }
    }

    public Optional<Job> cancel(String id) {
        Optional<Job> opt = getById(id);
        if(opt.isEmpty()) {
            return Optional.empty();
        }
        Job job = opt.get();

        if(job.getStatus() == Job.JobStatus.RUNNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Job is RUNNING and cannot be cancelled");
        }

        if(job.getStatus() == Job.JobStatus.PENDING) {
            job.setStatus(Job.JobStatus.CANCELLED);
            jobRepository.saveAndFlush(job);
            metrics.jobCancelled(job.getType());
        }

        return Optional.of(job);
    }
}
