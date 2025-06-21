// MyPageController.java
package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import com.nyumtolic.nyumtolic.service.RecommendationService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final ReviewService reviewService;
    private final RecommendationService recommendationService;
    private final RestaurantService restaurantService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        SiteUser user = principalDetails.getSiteUser();
        model.addAttribute("user", user);

        // 사용자가 작성한 리뷰 목록
        model.addAttribute("reviews", reviewService.getUserReviews(user.getId()));

        // AI 기반 개인화 추천 결과 가져오기
        try {
            if (recommendationService.isRecommendationServerHealthy()) {
                List<RecommendationService.RecommendationResult> recommendations =
                        recommendationService.getRecommendationsForUser(user.getId(), 5);

                if (!recommendations.isEmpty()) {
                    // 추천된 레스토랑 ID들로 실제 레스토랑 정보 조회
                    List<RestaurantRecommendation> restaurantRecommendations = new ArrayList<>();

                    for (RecommendationService.RecommendationResult rec : recommendations) {
                        try {
                            // null 체크
                            if (rec.getRestaurantId() == null) {
                                log.warn("추천 결과에서 레스토랑 ID가 null입니다: {}", rec);
                                continue;
                            }

                            Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantsById(rec.getRestaurantId());
                            if (restaurantOpt.isPresent()) {
                                Restaurant restaurant = restaurantOpt.get();
                                RestaurantRecommendation recItem = new RestaurantRecommendation();
                                recItem.setRestaurant(restaurant);
                                recItem.setRecommendationScore(rec.getScore());
                                recItem.setReason(generateRecommendationReason(rec.getScore()));
                                restaurantRecommendations.add(recItem);
                            } else {
                                log.warn("추천된 레스토랑 ID {}가 데이터베이스에 존재하지 않습니다.", rec.getRestaurantId());
                            }
                        } catch (Exception e) {
                            log.error("레스토랑 ID {} 조회 중 오류 발생: {}", rec.getRestaurantId(), e.getMessage());
                            continue;
                        }
                    }

                    log.info("총 {} 개의 추천 중 {} 개의 유효한 레스토랑 추천 결과 생성",
                            recommendations.size(), restaurantRecommendations.size());

                    if (!restaurantRecommendations.isEmpty()) {
                        model.addAttribute("aiRecommendations", restaurantRecommendations);
                        model.addAttribute("hasAiRecommendations", true);
                        log.info("사용자 {}에 대한 AI 추천 결과 {} 개 로드 완료", user.getId(), restaurantRecommendations.size());
                    } else {
                        model.addAttribute("hasAiRecommendations", false);
                        model.addAttribute("aiMessage", "추천된 음식점들이 현재 이용 불가능합니다. 잠시 후 다시 시도해주세요.");
                        log.warn("사용자 {}의 추천 결과는 있지만 유효한 레스토랑이 없습니다.", user.getId());
                    }
                } else {
                    model.addAttribute("hasAiRecommendations", false);
                    model.addAttribute("aiMessage", "아직 추천할 데이터가 부족합니다. 더 많은 리뷰를 작성해보세요!");
                }
            } else {
                model.addAttribute("hasAiRecommendations", false);
                model.addAttribute("aiMessage", "추천 서비스를 일시적으로 사용할 수 없습니다.");
                log.warn("추천 서버가 응답하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("AI 추천 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("hasAiRecommendations", false);
            model.addAttribute("aiMessage", "추천 결과를 불러오는 중 오류가 발생했습니다.");
        }

        // 유사한 사용자 정보 (선택적)
        try {
            List<RecommendationService.SimilarUser> similarUsers =
                    recommendationService.getSimilarUsers(user.getId(), 3);
            model.addAttribute("similarUsers", similarUsers);
        } catch (Exception e) {
            log.warn("유사 사용자 조회 실패: {}", e.getMessage());
        }

        // 추천 통계 정보
        model.addAttribute("recommendationStats", generateRecommendationStats(user));

        return "mypage";  // mypage.html 타임리프 템플릿 반환
    }

    /**
     * 추천 점수에 따른 이유 생성
     */
    private String generateRecommendationReason(Double score) {
        if (score >= 4.5) {
            return "당신의 취향에 완벽히 맞는 음식점입니다!";
        } else if (score >= 4.0) {
            return "당신이 좋아할 만한 음식점입니다.";
        } else if (score >= 3.5) {
            return "비슷한 취향의 사용자들이 추천하는 음식점입니다.";
        } else if (score >= 3.0) {
            return "새로운 맛을 경험해보세요.";
        } else {
            return "도전해볼 만한 음식점입니다.";
        }
    }

    /**
     * 사용자별 추천 통계 생성
     */
    private Map<String, Object> generateRecommendationStats(SiteUser user) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 사용자가 작성한 리뷰 수
            int reviewCount = reviewService.getUserReviews(user.getId()).size();
            stats.put("reviewCount", reviewCount);

            // 리뷰 기반 추천 가능 여부
            stats.put("canRecommend", reviewCount >= 3);

            // 추천 시스템 상태
            stats.put("systemAvailable", recommendationService.isRecommendationServerHealthy());

            // 권장 리뷰 수
            int recommendedReviews = Math.max(0, 5 - reviewCount);
            stats.put("recommendedReviews", recommendedReviews);

        } catch (Exception e) {
            log.warn("추천 통계 생성 실패: {}", e.getMessage());
            stats.put("error", true);
        }

        return stats;
    }

    // 추천 결과를 위한 내부 클래스
    public static class RestaurantRecommendation {
        private Restaurant restaurant;
        private Double recommendationScore;
        private String reason;

        // Getters and Setters
        public Restaurant getRestaurant() {
            return restaurant;
        }

        public void setRestaurant(Restaurant restaurant) {
            this.restaurant = restaurant;
        }

        public Double getRecommendationScore() {
            return recommendationScore;
        }

        public void setRecommendationScore(Double recommendationScore) {
            this.recommendationScore = recommendationScore;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        /**
         * 추천 점수를 5점 만점으로 환산
         */
        public String getScoreDisplay() {
            if (recommendationScore == null) return "0.0";
            return String.format("%.1f", Math.min(5.0, Math.max(0.0, recommendationScore)));
        }

        /**
         * 추천 점수에 따른 별 아이콘 클래스
         */
        public String getStarClass() {
            if (recommendationScore == null) return "text-muted";
            if (recommendationScore >= 4.5) return "text-warning";
            if (recommendationScore >= 4.0) return "text-info";
            if (recommendationScore >= 3.5) return "text-primary";
            return "text-secondary";
        }
    }
}