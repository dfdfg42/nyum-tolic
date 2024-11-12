package com.nyumtolic.nyumtolic.service;


import com.nyumtolic.nyumtolic.domain.VisitLog;
import com.nyumtolic.nyumtolic.repository.VisitLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitLogService {

    private final VisitLogRepository visitLogRepository;

    public VisitLogService(VisitLogRepository visitLogRepository) {
        this.visitLogRepository = visitLogRepository;
    }

    public void logVisit(Long userId, Long restaurantId) {
        VisitLog visitLog = new VisitLog();
        visitLog.setUserId(userId);
        visitLog.setRestaurantId(restaurantId);
        visitLog.setVisitedAt(LocalDateTime.now());  // 현재 시간을 방문 일시로 설정
        visitLogRepository.save(visitLog);
    }

    public List<VisitLog> getAllVisitLogs() {
        return visitLogRepository.findAll();  // 모든 방문 기록 반환
    }

    public List<VisitLog> getVisitLogsByUserId(Long userId) {
        return visitLogRepository.findByUserId(userId);  // 특정 사용자의 방문 기록 반환
    }
}
