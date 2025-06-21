package com.nyumtolic.nyumtolic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RestTemplate restTemplate;
    private final RestaurantService restaurantService; // 추가

    @Value("${recommendation.server.url:http://localhost:5000}")
    private String recommendationServerUrl;

    // 추천 결과 캐시 (메모리 캐시)
    private Map<Long, List<RecommendationResult>> recommendationCache = new HashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 30 * 60 * 1000; // 30분

    /**
     * 사용자별 추천 레스토랑 조회
     */
    public List<RecommendationResult> getRecommendationsForUser(Long userId, int topN) {
        try {
            // 캐시 확인
            if (isCacheValid() && recommendationCache.containsKey(userId)) {
                log.info("캐시에서 사용자 {}의 추천 결과 반환", userId);
                return recommendationCache.get(userId);
            }

            // 유효한 레스토랑 ID 목록 가져오기
            List<Long> validRestaurantIds = getValidRestaurantIds();

            String url = String.format("%s/recommend/%d?top_n=%d", recommendationServerUrl, userId, topN);

            // 유효한 레스토랑 ID를 파라미터로 전달
            if (!validRestaurantIds.isEmpty()) {
                String restaurantIdsParam = validRestaurantIds.stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","));
                url += "&restaurant_ids=" + restaurantIdsParam;
            }

            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    RecommendationResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<RecommendationResult> results = response.getBody().getRecommendations();

                // 결과 유효성 검증 및 필터링
                List<RecommendationResult> validResults = results.stream()
                        .filter(result -> result.getRestaurantId() != null && result.getRestaurantId() > 0)
                        .filter(result -> validRestaurantIds.contains(result.getRestaurantId()))
                        .collect(java.util.stream.Collectors.toList());

                // 캐시 업데이트
                recommendationCache.put(userId, validResults);
                updateCacheTimestamp();

                log.info("사용자 {}에 대한 유효한 추천 결과 {} 개 조회 완료", userId, validResults.size());
                return validResults;
            }

        } catch (Exception e) {
            log.error("사용자 {}의 추천 조회 중 오류 발생: {}", userId, e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * 배치 추천 조회 (여러 사용자)
     */
    public Map<Long, List<RecommendationResult>> getBatchRecommendations(List<Long> userIds, int topN) {
        try {
            String url = String.format("%s/recommend/batch", recommendationServerUrl);

            BatchRecommendationRequest request = new BatchRecommendationRequest();
            request.setUserIds(userIds);
            request.setTopN(topN);

            ResponseEntity<BatchRecommendationResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    BatchRecommendationResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, List<RecommendationResult>> results = response.getBody().getResults();

                // String key를 Long으로 변환
                Map<Long, List<RecommendationResult>> convertedResults = new HashMap<>();
                results.forEach((key, value) -> {
                    try {
                        Long userId = Long.parseLong(key);
                        convertedResults.put(userId, value);

                        // 캐시 업데이트
                        recommendationCache.put(userId, value);
                    } catch (NumberFormatException e) {
                        log.warn("사용자 ID 변환 실패: {}", key);
                    }
                });

                updateCacheTimestamp();
                log.info("배치 추천 조회 완료 - 사용자 수: {}", convertedResults.size());
                return convertedResults;
            }

        } catch (Exception e) {
            log.error("배치 추천 조회 중 오류 발생: {}", e.getMessage());
        }

        return Collections.emptyMap();
    }

    /**
     * 유사한 사용자 찾기
     */
    public List<SimilarUser> getSimilarUsers(Long userId, int topK) {
        try {
            String url = String.format("%s/similarity/user/%d?top_k=%d", recommendationServerUrl, userId, topK);

            ResponseEntity<SimilarUserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    SimilarUserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getSimilarUsers();
            }

        } catch (Exception e) {
            log.error("유사 사용자 조회 중 오류 발생: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * 유사한 레스토랑 찾기
     */
    public List<SimilarRestaurant> getSimilarRestaurants(Long restaurantId, int topK) {
        try {
            String url = String.format("%s/similarity/item/%d?top_k=%d", recommendationServerUrl, restaurantId, topK);

            ResponseEntity<SimilarRestaurantResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    SimilarRestaurantResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getSimilarRestaurants();
            }

        } catch (Exception e) {
            log.error("유사 레스토랑 조회 중 오류 발생: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * AI 서버 상태 체크
     */
    public boolean isRecommendationServerHealthy() {
        try {
            String url = String.format("%s/health", recommendationServerUrl);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.warn("추천 서버 상태 체크 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * AI 모델 재로드 요청
     */
    @Async
    public CompletableFuture<Boolean> reloadModels() {
        try {
            String url = String.format("%s/reload", recommendationServerUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                // 캐시 초기화
                clearCache();
                log.info("AI 모델 재로드 완료");
            }

            return CompletableFuture.completedFuture(success);

        } catch (Exception e) {
            log.error("AI 모델 재로드 실패: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 주기적 캐시 정리 (1시간마다)
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupCache() {
        if (!isCacheValid()) {
            clearCache();
            log.info("추천 캐시 정리 완료");
        }
    }

    /**
     * 캐시 유효성 검사
     */
    private boolean isCacheValid() {
        return System.currentTimeMillis() - lastCacheUpdate < CACHE_DURATION;
    }

    /**
     * 캐시 초기화
     */
    private void clearCache() {
        recommendationCache.clear();
        lastCacheUpdate = 0;
    }

    /**
     * 캐시 타임스탬프 업데이트
     */
    private void updateCacheTimestamp() {
        lastCacheUpdate = System.currentTimeMillis();
    }

    /**
     * 유효한 레스토랑 ID 목록 조회
     */
    private List<Long> getValidRestaurantIds() {
        try {
            return restaurantService.getAllRestaurants().stream()
                    .map(restaurant -> restaurant.getId())
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("유효한 레스토랑 ID 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // DTO 클래스들
    public static class RecommendationResult {
        private Long restaurantId;
        private Double score;

        // Getters and Setters
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }

    public static class RecommendationResponse {
        private Long userId;
        private List<RecommendationResult> recommendations;
        private Integer count;
        private String timestamp;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public List<RecommendationResult> getRecommendations() { return recommendations; }
        public void setRecommendations(List<RecommendationResult> recommendations) { this.recommendations = recommendations; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class BatchRecommendationRequest {
        private List<Long> userIds;
        private Integer topN;
        private List<Long> restaurantIds;

        // Getters and Setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public Integer getTopN() { return topN; }
        public void setTopN(Integer topN) { this.topN = topN; }
        public List<Long> getRestaurantIds() { return restaurantIds; }
        public void setRestaurantIds(List<Long> restaurantIds) { this.restaurantIds = restaurantIds; }
    }

    public static class BatchRecommendationResponse {
        private Map<String, List<RecommendationResult>> results;
        private String timestamp;

        // Getters and Setters
        public Map<String, List<RecommendationResult>> getResults() { return results; }
        public void setResults(Map<String, List<RecommendationResult>> results) { this.results = results; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class SimilarUser {
        private Long userId;
        private Double similarity;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Double getSimilarity() { return similarity; }
        public void setSimilarity(Double similarity) { this.similarity = similarity; }
    }

    public static class SimilarUserResponse {
        private Long userId;
        private List<SimilarUser> similarUsers;
        private String timestamp;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public List<SimilarUser> getSimilarUsers() { return similarUsers; }
        public void setSimilarUsers(List<SimilarUser> similarUsers) { this.similarUsers = similarUsers; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class SimilarRestaurant {
        private Long restaurantId;
        private Double similarity;

        // Getters and Setters
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        public Double getSimilarity() { return similarity; }
        public void setSimilarity(Double similarity) { this.similarity = similarity; }
    }

    public static class SimilarRestaurantResponse {
        private Long restaurantId;
        private List<SimilarRestaurant> similarRestaurants;
        private String timestamp;

        // Getters and Setters
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        public List<SimilarRestaurant> getSimilarRestaurants() { return similarRestaurants; }
        public void setSimilarRestaurants(List<SimilarRestaurant> similarRestaurants) { this.similarRestaurants = similarRestaurants; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}