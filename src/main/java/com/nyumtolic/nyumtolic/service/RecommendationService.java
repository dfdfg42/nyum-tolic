package com.nyumtolic.nyumtolic.service;

// RecommendationService.java

import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RecommendationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FLASK_RECOMMEND_URL = "http://localhost:5000/recommendations";

    private Map<String, Map<String, Double>> recommendations;

    // 매일 자정에 추천 결과를 갱신합니다.
    @Scheduled(cron = "0 0 0 * * *")
    public void updateRecommendations() {
        fetchRecommendations();
    }

    // 어드민이 수동으로 추천 결과를 갱신할 수 있도록 공개 메서드로 제공합니다.
    public void fetchRecommendations() {
        ResponseEntity<Map<String, Map<String, Double>>> response = restTemplate.exchange(
                FLASK_RECOMMEND_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Map<String, Double>>>(){}
        );
        recommendations = response.getBody();
    }

    // 추천 결과를 반환합니다.
    public Map<String, Map<String, Double>> getRecommendations() {
        return recommendations;
    }



}
