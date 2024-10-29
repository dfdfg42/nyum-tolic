package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final S3Service s3Service;
    private final ReviewLogRepository reviewLogRepository;

    public void vote(Review review, SiteUser siteUser) {
        review.getVoter().add(siteUser);
        this.reviewRepository.save(review);
    }

    public Review create(Long restaurantId, String content, Double rating, SiteUser author, MultipartFile image) throws IOException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new DataNotFoundException("Restaurant not found"));
        Review review = new Review();
        review.setContent(content);
        review.setCreateDate(LocalDateTime.now());
        review.setRating(rating);
        review.setRestaurant(restaurant);
        review.setAuthor(author);

        // Handle image upload
        String imageUrl = uploadImage(image);
        review.setImageUrl(imageUrl);

        reviewRepository.save(review);
        updateRestaurantUserRating(restaurantId);

        // 리뷰 로그 생성 및 저장
        ReviewLog reviewLog = new ReviewLog();
        reviewLog.setCreatedAt(LocalDateTime.now());
        reviewLog.setContent(content);
        reviewLog.setRating(rating);
        reviewLog.setAuthor(author);
        reviewLog.setRestaurantId(restaurantId);
        reviewLogRepository.save(reviewLog);

        return review;
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Review not found"));
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return getReview(id);
    }

    @Transactional
    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    public void modify(Review review, String content, Double rating, MultipartFile image) throws IOException {
        review.setContent(content);
        review.setModifyDate(LocalDateTime.now());
        review.setRating(rating);

        if (image != null && !image.isEmpty()) {
            deleteImageIfExists(review);
            String imageUrl = uploadImage(image);
            review.setImageUrl(imageUrl);
        }

        reviewRepository.save(review);
        updateRestaurantUserRating(review.getRestaurant().getId());
    }

    public void delete(Review review) {
        try {
            deleteImageIfExists(review);
        } catch (IOException e) {
            // Log the exception (optional)
        }
        reviewRepository.delete(review);
        updateRestaurantUserRating(review.getRestaurant().getId());
    }

    public List<Review> findReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    public Page<ReviewWithVotesDTO> findReviewsWithVotesByRestaurantId(Long restaurantId, Pageable pageable) {
        Page<Object[]> results = reviewRepository.findReviewsAndVoteCountByRestaurantId(restaurantId, pageable);
        List<ReviewWithVotesDTO> reviewWithVotesDTOs = results.getContent().stream()
                .map(result -> new ReviewWithVotesDTO((Review) result[0], (Long) result[1]))
                .collect(Collectors.toList());

        return new PageImpl<>(reviewWithVotesDTOs, pageable, results.getTotalElements());
    }

    @Transactional
    public void updateRestaurantUserRating(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);
        double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0); // Default to 0 if no reviews

        averageRating = Math.round(averageRating * 10) / 10.0;

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        restaurant.setUserRating(averageRating);
        restaurantRepository.save(restaurant);
    }

    public void deleteReviewImage(Long reviewId) throws IOException {
        Review review = getReview(reviewId);
        deleteImageIfExists(review);
        reviewRepository.save(review);
    }

    // Helper Methods

    private String uploadImage(MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            String fileName = "review/" + UUID.randomUUID().toString();
            return s3Service.uploadFileWithName(image, fileName);
        }
        return null;
    }

    private void deleteImageIfExists(Review review) throws IOException {
        if (review.getImageUrl() != null) {
            s3Service.deleteFileByURL(review.getImageUrl());
            review.setImageUrl(null);
        }
    }
}
