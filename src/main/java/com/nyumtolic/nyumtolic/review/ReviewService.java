package com.nyumtolic.nyumtolic.review;


import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    public void vote(Review review, SiteUser siteUser) {
        review.getVoter().add(siteUser);
        this.reviewRepository.save(review);
    }

    public Review create(Long restaurantId, String content, SiteUser author) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new DataNotFoundException("Restaurant not found"));
        Review review = new Review();
        review.setContent(content);
        review.setCreateDate(LocalDateTime.now());
        review.setRestaurant(restaurant);
        review.setAuthor(author);
        reviewRepository.save(review);
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

    public void modify(Review review, String content) {
        review.setContent(content);
        review.setModifyDate(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public void delete(Review answer) {
        this.reviewRepository.delete(answer);
    }

    public List<Review> findReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

}
