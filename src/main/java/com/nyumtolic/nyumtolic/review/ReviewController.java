package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.user.SiteUser;
import com.nyumtolic.nyumtolic.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RequestMapping("/review")
@RequiredArgsConstructor
@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Review>> getReviewsByRestaurantId(@PathVariable Long restaurantId) {
        List<Review> reviews = reviewService.findReviewsByRestaurantId(restaurantId);
        if(reviews.isEmpty()) {
            return ResponseEntity.noContent().build(); // 내용이 없을 때 204 상태 코드 반환
        }
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{restaurantId}")
    public ResponseEntity<?> createReview(@PathVariable("restaurantId") Long restaurantId,
                                          @RequestBody ReviewForm reviewForm,
                                          Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        try {
            Review review = reviewService.create(restaurantId, reviewForm.getContent(), siteUser);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create review");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{reviewId}")
    public String reviewModify(ReviewForm reviewForm, @PathVariable("reviewId") Long reviewId, Principal principal) {
        Review review = this.reviewService.getReview(reviewId);
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        reviewForm.setContent(review.getContent());
        return "review_form"; // 리뷰 수정 폼으로 가정함. 실제 뷰 이름에 맞게 수정 필요
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{reviewId}")
    public String reviewModify(@Valid ReviewForm reviewForm, BindingResult bindingResult,
                               @PathVariable("reviewId") Long reviewId, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "review_form"; // 오류 시 리뷰 수정 폼으로 반환
        }
        Review review = this.reviewService.getReview(reviewId);
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.reviewService.modify(review, reviewForm.getContent());
        return String.format("redirect:/restaurant/detail/%s#review_%s", review.getRestaurant().getId(), review.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{reviewId}")
    public String reviewDelete(Principal principal, @PathVariable("reviewId") Long reviewId) {
        Review review = this.reviewService.getReview(reviewId);
        if (!review.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.reviewService.delete(review);
        return String.format("redirect:/restaurant/detail/%s", review.getRestaurant().getId());
    }

}
