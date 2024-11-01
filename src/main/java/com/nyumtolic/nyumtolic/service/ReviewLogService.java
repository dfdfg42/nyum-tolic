package com.nyumtolic.nyumtolic.service;

import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewLogService {

    private final ReviewLogRepository reviewLogRepository;

    public ReviewLogService(ReviewLogRepository reviewLogRepository) {
        this.reviewLogRepository = reviewLogRepository;
    }

    public List<ReviewLog> getAllReviewLogs() {
        return reviewLogRepository.findAll();  // 모든 리뷰 기록 반환
    }

    public List<ReviewLog> getReviewLogsByUserId(Long userId) {
        return reviewLogRepository.findByAuthor_Id(userId);  // 특정 사용자의 리뷰 기록 반환
    }
}
