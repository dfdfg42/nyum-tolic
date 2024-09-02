package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
    @Query("SELECT r FROM Restaurant r ORDER BY COALESCE(r.userRating, 0) DESC")
    List<Restaurant> findAllOrderByUserRating();

    @Query("SELECT r FROM Restaurant r ORDER BY r.name")
    List<Restaurant> findAllOrderByName();

    @Query("SELECT r FROM Restaurant r JOIN r.categories c WHERE c.id = :categoryId ORDER BY r.id")
    List<Restaurant> findAllByCategoryId(Long categoryId);

    // 우선순위 1. 양수의 높은 별점 2. 미평가별점 or NULL별점(DB에 의존)
    @Query("SELECT r FROM Restaurant r JOIN r.categories c WHERE c.id = :categoryId ORDER BY COALESCE(r.userRating, 0) DESC")
    List<Restaurant> findAllByCategoryIdOrderByUserRating(Long categoryId);

    @Query("SELECT r FROM Restaurant r JOIN r.categories c WHERE c.id = :categoryId ORDER BY r.name")
    List<Restaurant> findAllByCategoryIdOrderByName(Long categoryId);

    Optional<Restaurant> findByName(String name);

}


