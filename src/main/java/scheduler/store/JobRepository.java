package scheduler.store;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import scheduler.domain.Job;
import org.springframework.data.domain.Pageable;import java.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByExternalId(UUID externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.status = 'PENDING' AND j.scheduledAt <= :now " +
            "ORDER BY j.scheduledAt ASC")
    List<Job> findPendingJobs(@Param("now")Instant now, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'RUNNING' AND j.claimedAt < :threshold")
    List<Job> findStaleRunningJobs(@Param("threshold") Instant threshold);
}
