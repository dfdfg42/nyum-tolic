package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 검색 관련 메서드 추가
    @Query("SELECT DISTINCT r FROM Restaurant r LEFT JOIN r.menu m WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY r.name")
    List<Restaurant> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT r FROM Restaurant r LEFT JOIN r.menu m WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY COALESCE(r.userRating, 0) DESC")
    List<Restaurant> findByKeywordOrderByUserRating(@Param("keyword") String keyword);

    // 카테고리와 검색어를 함께 사용하는 메서드
    @Query("SELECT DISTINCT r FROM Restaurant r LEFT JOIN r.menu m JOIN r.categories c WHERE " +
            "c.id = :categoryId AND (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY r.name")
    List<Restaurant> findByCategoryIdAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT r FROM Restaurant r LEFT JOIN r.menu m JOIN r.categories c WHERE " +
            "c.id = :categoryId AND (" +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY COALESCE(r.userRating, 0) DESC")
    List<Restaurant> findByCategoryIdAndKeywordOrderByUserRating(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
}