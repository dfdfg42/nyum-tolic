package com.nyumtolic.nyumtolic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nyumtolic.nyumtolic.domain.VisitLog;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.repository.VisitLogRepository;
import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
@Service
@RequiredArgsConstructor
public class AIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FLASK_URL = "http://localhost:5000";

    public InputStreamResource getRecommendationsCSV() {
        String url = FLASK_URL + "/recommendations";
        try {
            ResponseEntity<InputStreamResource> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    InputStreamResource.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get CSV file from AI service");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching CSV file from AI service", e);
        }
    }
}
