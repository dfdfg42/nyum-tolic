package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.S3.S3Service;
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
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    private final S3Service s3Service;

    public void vote(Review review, SiteUser siteUser) {
        review.getVoter().add(siteUser);
        this.reviewRepository.save(review);
    }


    public Review create(Long restaurantId, String content, Double rating, SiteUser author, String imageUrl) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new DataNotFoundException("Restaurant not found"));
        Review review = new Review();
        review.setContent(content);
        review.setCreateDate(LocalDateTime.now());
        review.setRating(rating);
        review.setRestaurant(restaurant);
        review.setAuthor(author);

        review.setImageUrl(imageUrl);

        reviewRepository.save(review);
        updateRestaurantUserRating(restaurantId);
        return review;
    }

    public Review getReview(Long id) {
        Optional<Review> review = this.reviewRepository.findById(id);
        if (review.isPresent()) {
            return review.get();
        } else {
            throw new DataNotFoundException("review not found");
        }
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review id: " + id));
    }

    @Transactional
    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    public void modify(Review review, String content, Double rating, String imageUrl) {
        review.setContent(content);
        review.setModifyDate(LocalDateTime.now());
        review.setRating(rating);
        review.setImageUrl(imageUrl);
        this.reviewRepository.save(review);
        updateRestaurantUserRating(review.getRestaurant().getId());
    }


    public void delete(Review review) {
        if (review.getImageUrl() != null) {
            String fileName = review.getImageUrl().substring(review.getImageUrl().lastIndexOf("/") + 1);
            s3Service.deleteFile(fileName);
        }
        this.reviewRepository.delete(review);
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
                .orElse(0.0); // 리뷰가 없는 경우 기본값으로 0임.

        averageRating = Math.round(averageRating * 10) / 10.0;

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        restaurant.setUserRating(averageRating);
        restaurantRepository.save(restaurant);
    }

    public void saveReviewImage(Long reviewId, MultipartFile image) throws IOException {
        Review review = getReview(reviewId);
        if (review.getImageUrl() != null) {
            String fileName = review.getImageUrl().substring(review.getImageUrl().lastIndexOf("/") + 1);
            s3Service.deleteFile(fileName);
        }
        String imageUrl = s3Service.uploadFile(image);
        review.setImageUrl(imageUrl);
        reviewRepository.save(review);
    }

    public void deleteReviewImage(Long reviewId) {
        Review review = getReview(reviewId);
        if (review.getImageUrl() != null) {
            String fileName = review.getImageUrl().substring(review.getImageUrl().lastIndexOf("/") + 1);
            s3Service.deleteFile(fileName);
            review.setImageUrl(null);
            reviewRepository.save(review);
        }
    }
}

