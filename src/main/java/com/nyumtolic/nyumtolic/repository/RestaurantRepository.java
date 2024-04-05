package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {

    @Query("SELECT r FROM Restaurant r JOIN r.categories c WHERE c.id = :categoryId ORDER BY r.id")
    List<Restaurant> findAllByCategoryId(Long categoryId);

    @Query("SELECT r FROM Restaurant r JOIN r.categories c WHERE c.id = :categoryId ORDER BY r.userRating DESC nulls last ")
    List<Restaurant> findAllByCategoryIdOrderByUserRating(Long categoryId);

    @Query("SELECT r FROM Restaurant r JOIN r.categories c WHERE c.id = :categoryId ORDER BY r.name")
    List<Restaurant> findAllByCategoryIdOrderByName(Long categoryId);

}


