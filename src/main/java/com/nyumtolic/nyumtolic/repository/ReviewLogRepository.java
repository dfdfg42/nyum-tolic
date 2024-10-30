package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
    List<ReviewLog> findByUserId(Long userId);  // 특정 사용자의 리뷰 기록 조회

    List<ReviewLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
