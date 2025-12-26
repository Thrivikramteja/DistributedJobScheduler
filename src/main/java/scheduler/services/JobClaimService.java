package scheduler.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scheduler.config.SchedulerMetrics;
import scheduler.domain.Job;
import scheduler.store.JobRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class JobClaimService {

    private final JobRepository jobRepository;
    private final SchedulerMetrics metrics;

    public JobClaimService(JobRepository jobRepository, SchedulerMetrics metrics) {
        this.jobRepository = jobRepository;
        this.metrics = metrics;
    }

    @Transactional
    public Optional<Job> claimOne(String workerId) {
        List<Job> jobsList = jobRepository
                .findPendingJobs(Instant.now(), PageRequest.of(0, 1));
        if(jobsList.isEmpty()) {
            return Optional.empty();
        }
        Job job = jobsList.getFirst();
        job.setStatus(Job.JobStatus.RUNNING);
        job.setWorkerId(workerId);
        job.setClaimedAt(Instant.now());
        job.setStartedAt(Instant.now());
        jobRepository.saveAndFlush(job);
        metrics.jobClaimed(job.getType());
        return Optional.of(job);
    }
}
