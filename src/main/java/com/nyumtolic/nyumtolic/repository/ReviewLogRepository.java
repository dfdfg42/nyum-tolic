package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
    List<ReviewLog> findByAuthor_Id(Long userId);

    List<ReviewLog> findByAuthor_IdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);


}
