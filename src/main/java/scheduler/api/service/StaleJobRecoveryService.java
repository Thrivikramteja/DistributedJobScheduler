package scheduler.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scheduler.config.SchedulerMetrics;
import scheduler.domain.Job;
import scheduler.store.JobRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StaleJobRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(StaleJobRecoveryService.class);
    private final JobRepository jobRepository;
    private final SchedulerMetrics metrics;

    @Value("${scheduler.stale-threshold-minutes:15}")
    private int staleThresholdMinutes;

    public StaleJobRecoveryService(JobRepository jobRepository, SchedulerMetrics metrics) {
        this.jobRepository = jobRepository;
        this.metrics = metrics;
    }

    @Transactional
    public void releaseStaleJobs() {
        Instant threshold = Instant.now().minus(staleThresholdMinutes, ChronoUnit.MINUTES);
        List<Job> stale = jobRepository.findStaleRunningJobs(threshold);
        for(Job job : stale) {
            job.setStatus(Job.JobStatus.PENDING);
            job.setWorkerId(null);
            job.setStartedAt(null);
            job.setClaimedAt(null);
            jobRepository.saveAndFlush(job);
            metrics.jobStaleReleased();
        }
        if(!stale.isEmpty()) {
            log.info("Released {} stale RUNNING jobs", stale.size());
        }
    }
}
