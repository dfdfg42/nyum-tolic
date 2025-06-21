package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByAuthor(SiteUser author);

    List<Review> findByAuthorId(Long userId);

    @Query("SELECT r, COUNT(v) FROM Review r LEFT JOIN r.voter v WHERE r.restaurant.id = :restaurantId GROUP BY r.id ORDER BY COUNT(v) DESC,r.modifyDate DESC ,r.createDate DESC")
    Page<Object[]> findReviewsAndVoteCountByRestaurantId(@Param("restaurantId") Long restaurantId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.restaurant JOIN FETCH r.author WHERE r.author.id = :userId ORDER BY r.createDate DESC")
    List<Review> findByAuthorIdWithRestaurant(@Param("userId") Long userId);

}
