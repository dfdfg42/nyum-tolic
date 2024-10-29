package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageController {

    private final ReviewService reviewService;

    public MyPageController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        SiteUser user = principalDetails.getSiteUser(); // PrincipalDetails에서 SiteUser 가져오기
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviewService.getUserReviews(user.getId()));

        return "mypage";  // mypage.html 타임리프 템플릿 반환
    }
}
