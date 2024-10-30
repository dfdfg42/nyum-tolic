package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.VisitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {
    List<VisitLog> findByUserId(Long userId);  // 특정 사용자의 방문 기록 조회
}