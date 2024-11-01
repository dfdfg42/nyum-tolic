package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final ReviewService reviewService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FLASK_RECOMMEND_URL = "http://localhost:5000/recommendations";

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        SiteUser user = principalDetails.getSiteUser();
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviewService.getUserReviews(user.getId()));

        // Flask 서버에서 추천 데이터 가져오기
        ResponseEntity<Map<String, Map<String, Double>>> response = restTemplate.exchange(
                FLASK_RECOMMEND_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Map<String, Double>>>(){}
        );

        Map<String, Map<String, Double>> recommendations = response.getBody();

        // 각 연도별로 점수가 높은 상위 3개의 레스토랑 추출
        Map<String, List<Map.Entry<String, Double>>> topRecommendations = recommendations.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))  // 점수 내림차순 정렬
                                .limit(3)  // 상위 3개만 선택
                                .collect(Collectors.toList())
                ));

        model.addAttribute("topRecommendations", topRecommendations);

        return "mypage";  // mypage.html 타임리프 템플릿 반환
    }
}
