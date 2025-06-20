package com.nyumtolic.nyumtolic.service;

import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.api.domain.CategoryDTO;
import com.nyumtolic.nyumtolic.api.domain.PageResponse;
import com.nyumtolic.nyumtolic.api.domain.RestaurantDTO;
import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final S3Service s3Service;

    // 저장
    public void save(Restaurant restaurant) {
        restaurantRepository.save(restaurant);
    }

    // 모든 Restaurant 리스트를 반환하는 메서드
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll(Sort.by("id"));
    }

    //음식점 버튼 정렬 (기본 list 페이지)
    public List<Restaurant> getAllRestaurantsBySorted(String sort) {
        if ("userRating".equals(sort)) { // 유저 별점 순 정렬, 내림차순
            return restaurantRepository.findAllOrderByUserRating();
        }
        return restaurantRepository.findAllOrderByName(); // 이름 정렬, 오름차순
    }

    public List<Restaurant> getAllByCategoryIdSorted(Long id, String sort) {
        if ("userRating".equals(sort)) {
            return restaurantRepository.findAllByCategoryIdOrderByUserRating(id);
        } else {
            return restaurantRepository.findAllByCategoryIdOrderByName(id);
        }
    }

    // 특정 id를 가진 Restaurant 반환하는 메서드
    public Optional<Restaurant> getRestaurantsById(Long id) {
        return restaurantRepository.findById(id);
    }

    // 특정 카테고리를 가진 Restaurant를 카테고리id로 조회해서 반환
    public List<Restaurant> findAllByCategoryId(Long categoryId) {
        return restaurantRepository.findAllByCategoryId(categoryId);
    }

    // 검색 기능 추가
    public List<Restaurant> searchRestaurants(String keyword, String sort) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRestaurantsBySorted(sort);
        }

        if ("userRating".equals(sort)) {
            return restaurantRepository.findByKeywordOrderByUserRating(keyword.trim());
        } else {
            return restaurantRepository.findByKeyword(keyword.trim());
        }
    }

    // 카테고리와 검색어를 함께 사용하는 메서드
    public List<Restaurant> searchRestaurantsByCategory(Long categoryId, String keyword, String sort) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllByCategoryIdSorted(categoryId, sort);
        }

        if ("userRating".equals(sort)) {
            return restaurantRepository.findByCategoryIdAndKeywordOrderByUserRating(categoryId, keyword.trim());
        } else {
            return restaurantRepository.findByCategoryIdAndKeyword(categoryId, keyword.trim());
        }
    }

    /**
     * 통합 검색 메서드 - 카테고리, 검색어, 정렬을 모두 고려
     */
    public List<Restaurant> searchRestaurantsWithFilters(Long categoryId, String keyword, String sort) {
        // 카테고리와 검색어 모두 있는 경우
        if (categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
            return searchRestaurantsByCategory(categoryId, keyword, sort);
        }
        // 검색어만 있는 경우
        else if (keyword != null && !keyword.trim().isEmpty()) {
            return searchRestaurants(keyword, sort);
        }
        // 카테고리만 있는 경우
        else if (categoryId != null) {
            return getAllByCategoryIdSorted(categoryId, sort);
        }
        // 둘 다 없는 경우 (전체 목록)
        else {
            return getAllRestaurantsBySorted(sort);
        }
    }

    /**
     * 특정 카테고리와 이미 제외된 ID를 제외하고 랜덤으로 하나 골라 반환
     */
    public Optional<Restaurant> recommendRandomRestaurantExcluding(String[] excludedCategories, Set<Long> excludedIds) {
        // 1. 후보 필터링
        List<Restaurant> candidates = getAllRestaurants().stream()
                .filter(r -> !isExcluded(r.getCategories(), excludedCategories))
                .filter(r -> !excludedIds.contains(r.getId()))
                .collect(Collectors.toList());

        // 2. 후보가 없다면 Optional.empty()
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        // 3. 랜덤 선택
        Restaurant chosen = candidates.get(new Random().nextInt(candidates.size()));
        return Optional.of(chosen);
    }

    private boolean isExcluded(List<Category> categories, String[] excludedCategories) {
        for (String ex : excludedCategories) {
            for (Category c : categories) {
                if (c.getName().equals(ex)) { // 정확 매칭 (대소문자 포함)
                    return true;
                }
            }
        }
        return false;
    }

    // 삭제
    public void delete(Restaurant restaurant) {
        restaurantRepository.delete(restaurant);
    }

    public void delete(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("음식점을 찾을 수 없습니다."));
        s3Service.deleteFileByURL(restaurant.getPhoto());
        restaurantRepository.deleteById(id);
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    // 이름으로 Restaurant 조회
    public Restaurant findByName(String name) {
        return restaurantRepository.findByName(name).orElse(null);
    }

    //// API 관련 서비스 메소드
    public PageResponse<RestaurantDTO> getAllRestaurantsDTO(Pageable pageable) {
        Page<RestaurantDTO> data = restaurantRepository.findAll(pageable).map(this::createRestaurantDTO);
        return new PageResponse<>(data);
    }

    public RestaurantDTO createRestaurantDTO(Restaurant restaurant) {
        List<CategoryDTO> categoryDTOs = restaurant.getCategories().stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .collect(Collectors.toList());

        return new RestaurantDTO(
                restaurant.getId(),
                restaurant.getPhoto(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhoneNumber(),
                categoryDTOs,
                restaurant.getRating(),
                restaurant.getMenu(),
                restaurant.getDescription(),
                restaurant.getTravelTime(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getUserRating()
        );
    }
}