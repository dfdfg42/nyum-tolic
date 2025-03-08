// MyPageController.java
package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import com.nyumtolic.nyumtolic.service.RecommendationService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

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
        model.addAttribute("reviews", reviewService.getUserReviews(user.getId()));

       /* // RecommendationService에서 추천 결과 가져오기
        Map<String, Map<String, Double>> recommendations = recommendationService.getRecommendations();

        // 추천 결과가 없을 경우 처리 (초기 서버 실행 후 추천 결과가 없을 수 있음)
        if (recommendations == null) {
            model.addAttribute("topRecommendations", Collections.emptyList());
            return "mypage";
        }

        // 해당 유저의 추천 결과 가져오기
        String userId = String.valueOf(user.getId());
        Map<String, Double> userRecommendations = recommendations.get(userId);

        if (userRecommendations == null) {
            model.addAttribute("topRecommendations", Collections.emptyList());
            return "mypage";
        }

        // 점수가 높은 상위 3개의 레스토랑 추출
        List<Map.Entry<String, Double>> topRecommendations = userRecommendations.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())  // 점수 내림차순 정렬
                .limit(3)  // 상위 3개만 선택
                .collect(Collectors.toList());

        model.addAttribute("topRecommendations", topRecommendations);

        List<Restaurant> restaurantList = restaurantService.getAllRestaurants();
        Map<String, Restaurant> restaurantMap = restaurantList.stream()
                .collect(Collectors.toMap(r -> String.valueOf(r.getId()), r -> r));
        model.addAttribute("restaurants", restaurantMap);*/

        return "mypage";  // mypage.html 타임리프 템플릿 반환
    }
}
