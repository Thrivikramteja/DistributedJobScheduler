package scheduler.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import scheduler.config.SchedulerMetrics;
import scheduler.domain.Job;
import scheduler.store.JobRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SchedulerMetrics metrics;

    @InjectMocks
    private JobService jobService;

    @Test
    void cancel_whenJobNotFound_returnsEmpty() {
        when(jobRepository.findByExternalId(any(UUID.class))).thenReturn(Optional.empty());

        Optional<Job> result = jobService.cancel("550e8400-e29b-41d4-a716-446655440000");

        assertThat(result).isEmpty();
    }

    @Test
    void cancel_whenJobRunning_throwsConflict() {
        Job job = new Job();
        job.setId(1L);
        job.setExternalId(UUID.randomUUID());
        job.setStatus(Job.JobStatus.RUNNING);
        when(jobRepository.findByExternalId(any(UUID.class))).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.cancel(job.getExternalId().toString()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException e = (ResponseStatusException) ex;
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void cancel_whenJobPending_setsCancelledAndSaves() {
        Job job = new Job();
        job.setId(1L);
        job.setExternalId(UUID.randomUUID());
        job.setStatus(Job.JobStatus.PENDING);

        when(jobRepository.findByExternalId(any(UUID.class))).thenReturn(Optional.of(job));
        when(jobRepository.saveAndFlush(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Job> result = jobService.cancel(job.getExternalId().toString());

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(Job.JobStatus.CANCELLED);
        verify(jobRepository).saveAndFlush(job);
    }
}
