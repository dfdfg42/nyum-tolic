package com.nyumtolic.nyumtolic.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import com.nyumtolic.nyumtolic.domain.VisitLog;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.repository.VisitLogRepository;
import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
public class AIService {

    private final VisitLogRepository visitLogRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FLASK_URL = "http://localhost:5000";

    public AIService(VisitLogRepository visitLogRepository, ReviewLogRepository reviewLogRepository) {
        this.visitLogRepository = visitLogRepository;
        this.reviewLogRepository = reviewLogRepository;
    }

    public String sendUserLogsToAI(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 특정 조건의 VisitLog와 ReviewLog 데이터를 모으기
        List<VisitLog> visitLogs = visitLogRepository.findByUserIdAndVisitedAtBetween(userId, startDate, endDate);
        List<ReviewLog> reviewLogs = reviewLogRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);

        // 데이터 전송
        String visitLogResponse = sendVisitLogsToAI(visitLogs);
        String reviewLogResponse = sendReviewLogsToAI(reviewLogs);

        return "VisitLog Response: " + visitLogResponse + ", ReviewLog Response: " + reviewLogResponse;
    }

    private String sendVisitLogsToAI(List<VisitLog> visitLogs) {
        String url = FLASK_URL + "/process-visit-logs";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<VisitLog>> request = new HttpEntity<>(visitLogs, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }

    private String sendReviewLogsToAI(List<ReviewLog> reviewLogs) {
        String url = FLASK_URL + "/process-review-logs";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<ReviewLog>> request = new HttpEntity<>(reviewLogs, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }
}
