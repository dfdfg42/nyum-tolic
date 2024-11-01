package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.api.domain.ReviewLogDTO;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.service.ReviewLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review-logs")
@RequiredArgsConstructor
public class ReviewLogController {

    private final ReviewLogService reviewLogService;

    @GetMapping
    public List<ReviewLogDTO> getAllReviewLogs() {
        return reviewLogService.getAllReviewLogs().stream()
                .map(ReviewLogDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public List<ReviewLog> getReviewLogsByUser(@PathVariable Long userId) {
        return reviewLogService.getReviewLogsByUserId(userId);  // 특정 사용자의 리뷰 기록
    }
}

