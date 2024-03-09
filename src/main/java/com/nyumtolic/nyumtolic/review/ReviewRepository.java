package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByAuthor(SiteUser author);
}
