package scheduler.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import scheduler.api.dto.CreateJobRequest;
import scheduler.api.dto.CreateJobResponse;
import scheduler.api.dto.JobStatusResponse;
import scheduler.api.service.JobService;
import scheduler.domain.Job;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateJobResponse create(@Valid @RequestBody CreateJobRequest request) {
        Job job = jobService.create(request);
        return new CreateJobResponse(job.getExternalId(), job.getStatus().name());
    }

    @GetMapping("/{id}")
    public JobStatusResponse getById(@PathVariable String id) {
        return jobService.getById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    @PatchMapping("/{id}/cancel")
    public JobStatusResponse cancelJob(@PathVariable String id) {
        return jobService.cancel(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Job not found"));
    }

    private JobStatusResponse toResponse(Job job) {
        return new JobStatusResponse(
                job.getExternalId(),
                job.getType(),
                job.getStatus().name(),
                job.getPayload(),
                job.getPriority(),
                job.getCreatedAt(),
                job.getScheduledAt(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getLastError()
        );
    }
}
