package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@RequestMapping("/review/admin")
@RequiredArgsConstructor
@Controller
public class ReviewAdminController {

    private final ReviewService reviewService;

    // 리뷰 삭제
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId) {
        Review review = reviewService.getReview(reviewId);
        Long restaurantId = review.getRestaurant().getId();
        reviewService.delete(review);
        return "redirect:/restaurant/detail/" + restaurantId;
    }
}
