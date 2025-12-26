package scheduler.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scheduler.api.service.StaleJobRecoveryService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class JobPollingScheduler {

    private final JobClaimService jobClaimService;
    private final JobExecutionService jobExecutionService;
    private final StaleJobRecoveryService staleJobRecoveryService;
    private final ExecutorService jobExecutor;
    private final String workerId;
    private static final Logger log = LoggerFactory.getLogger(JobPollingScheduler.class);

    public JobPollingScheduler(JobClaimService jobClaimService, JobExecutionService jobExecutionService, StaleJobRecoveryService staleJobRecoveryService, ExecutorService jobExecutor, @Qualifier("workerId") String workerId) {
        this.jobClaimService = jobClaimService;
        this.jobExecutionService = jobExecutionService;
        this.staleJobRecoveryService = staleJobRecoveryService;
        this.jobExecutor = jobExecutor;
        this.workerId = workerId;
    }

    @Scheduled(fixedDelayString = "${scheduler.poll.fixed-delay:5000}",
                timeUnit = TimeUnit.MILLISECONDS)
    public void poll() {
        jobClaimService.claimOne(workerId).ifPresent(job -> jobExecutor.submit(() ->
                jobExecutionService.execute(job)));
    }

    @Scheduled(fixedRateString = "${scheduler.stale-check-interval-ms:60000}")
    public void releaseStaleJobs() {
        staleJobRecoveryService.releaseStaleJobs();
    }
}
