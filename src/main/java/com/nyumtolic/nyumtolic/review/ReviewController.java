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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@RequestMapping("/review")
@RequiredArgsConstructor
@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    // Create Review
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{restaurantId}")
    public String createReview(@PathVariable Long restaurantId, @Valid ReviewForm reviewForm,
                               BindingResult bindingResult, Principal principal,
                               @RequestParam("image") MultipartFile image,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", bindingResult);
            redirectAttributes.addFlashAttribute("reviewForm", reviewForm);
            return "redirect:/restaurant/detail/" + restaurantId;
        }

        SiteUser siteUser = userService.getUser(principal.getName());

        try {
            reviewService.create(restaurantId, reviewForm.getContent(), reviewForm.getRating(), siteUser, image);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Image upload failed.");
            return "redirect:/restaurant/detail/" + restaurantId;
        }

        return "redirect:/restaurant/detail/" + restaurantId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{reviewId}")
    public String showModifyForm(@PathVariable Long reviewId, Model model, Principal principal) {
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this review.");
        }
        model.addAttribute("review", review);
        return "review_form";
    }

    // Modify Review
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{reviewId}")
    public String modifyReview(@PathVariable Long reviewId, @Valid ReviewForm reviewForm,
                               BindingResult bindingResult, Principal principal,
                               RedirectAttributes redirectAttributes,
                               @RequestParam("image") MultipartFile image) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", bindingResult);
            redirectAttributes.addFlashAttribute("reviewForm", reviewForm);
            return "redirect:/review/modify/" + reviewId;
        }
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this review.");
        }

        try {
            reviewService.modify(review, reviewForm.getContent(), reviewForm.getRating(), image);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Image upload failed.");
            return "redirect:/review/modify/" + reviewId;
        }

        return "redirect:/restaurant/detail/" + review.getRestaurant().getId();
    }

    // Delete Review
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, Principal principal) {
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this review.");
        }
        Long restaurantId = review.getRestaurant().getId();
        reviewService.delete(review);
        return "redirect:/restaurant/detail/" + restaurantId;
    }

    // Vote Review
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{reviewId}")
    public String reviewVote(Principal principal, @PathVariable Long reviewId) {
        Review review = reviewService.getReview(reviewId);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.reviewService.vote(review, siteUser);
        return "redirect:/restaurant/detail/" + review.getRestaurant().getId();
    }

    // Delete Review Image
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{reviewId}/image")
    public String deleteReviewImage(@PathVariable Long reviewId, Principal principal, RedirectAttributes redirectAttributes) {
        Review review = reviewService.getReview(reviewId);
        if (!review.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete the image for this review.");
        }
        try {
            reviewService.deleteReviewImage(reviewId);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Image deletion failed.");
            return "redirect:/restaurant/detail/" + review.getRestaurant().getId();
        }
        return "redirect:/restaurant/detail/" + review.getRestaurant().getId();
    }
}