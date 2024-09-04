package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@RequestMapping("/review")
@RequiredArgsConstructor
@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    // 리뷰 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{restaurantId}")
    public String createReview(@PathVariable Long restaurantId, @Valid ReviewForm reviewForm,
                               BindingResult bindingResult, Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // 오류 정보와 함께 리다이렉트
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", bindingResult);
            redirectAttributes.addFlashAttribute("reviewForm", reviewForm);
            return "redirect:/restaurant/detail/" + restaurantId;
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        reviewService.create(restaurantId, reviewForm.getContent(),reviewForm.getRating(), siteUser);
        return "redirect:/restaurant/detail/" + restaurantId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{reviewId}")
    public String showModifyForm(@PathVariable Long reviewId, Model model, Principal principal) {
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        model.addAttribute("review", review);
        return "review_form";
    }

    // 리뷰 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{reviewId}")
    public String modifyReview(@PathVariable Long reviewId, @Valid ReviewForm reviewForm,
                               BindingResult bindingResult, Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", bindingResult);
            redirectAttributes.addFlashAttribute("reviewForm", reviewForm);
            return "redirect:/review/modify/" + reviewId;
        }
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        reviewService.modify(review, reviewForm.getContent(), reviewForm.getRating());
        return "redirect:/restaurant/detail/" + review.getRestaurant().getId();
    }

    // 리뷰 삭제
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, Principal principal) {
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }
        Long restaurantId = review.getRestaurant().getId();
        reviewService.delete(review);
        return "redirect:/restaurant/detail/" + restaurantId;
    }

    //리뷰 추천
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{reviewId}")
    public String answerVote(Principal principal, @PathVariable Long reviewId) {
        Review review = reviewService.getReview(reviewId);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.reviewService.vote(review, siteUser);
        return "redirect:/restaurant/detail/" + review.getRestaurant().getId();
    }
}