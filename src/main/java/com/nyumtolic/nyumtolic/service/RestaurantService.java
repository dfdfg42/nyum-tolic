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


    /**
     * 특정 카테고리와 이전에 추천된 레스토랑을 제외하고 랜덤 레스토랑을 추천하는 향상된 메서드
     * 
     * @param excludedCategories 제외할 카테고리 배열
     * @param excludedIds 이전에 추천된 레스토랑 ID 집합
     * @param resetExclusions 모든 레스토랑이 제외되었을 때 제외 목록을 초기화할지 여부
     * @return 추천된 레스토랑
     */
    public Optional<Restaurant> recommendRandomRestaurantExcluding(
            String[] excludedCategories, 
            Set<Long> excludedIds,
            boolean resetExclusions) {
        
        // 1. 카테고리와 이전 추천 ID로 필터링
        List<Restaurant> candidates = getAllRestaurants().stream()
                .filter(r -> !isExcluded(r.getCategories(), excludedCategories))
                .filter(r -> !excludedIds.contains(r.getId()))
                .collect(Collectors.toList());

        // 2. 만약 모든 레스토랑이 제외되었고 초기화가 허용되면, 카테고리만 필터링
        if (candidates.isEmpty() && resetExclusions) {
            candidates = getAllRestaurants().stream()
                    .filter(r -> !isExcluded(r.getCategories(), excludedCategories))
                    .collect(Collectors.toList());
            
            // 이전 추천 목록 초기화
            excludedIds.clear();
        }

        // 3. 그래도 없으면 빈 결과 반환
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        // 4. 추천 알고리즘 - 가중치를 적용한 랜덤 선택
        // 현재는 단순 랜덤이지만 나중에 가중치를 적용할 수 있는 구조
        Random random = new Random();
        Restaurant chosen = candidates.get(random.nextInt(candidates.size()));
        
        return Optional.of(chosen);
    }
    
    /**
     * 하위 호환성을 위한 메서드
     */
    public Optional<Restaurant> recommendRandomRestaurantExcluding(
            String[] excludedCategories, Set<Long> excludedIds) {
        return recommendRandomRestaurantExcluding(excludedCategories, excludedIds, true);
    }

    // 카테고리 목록에 제외할 카테고리가 있는지 확인
    private boolean isExcluded(List<Category> categories, String[] excludedCategories) {
        for (String excludedCategory : excludedCategories) {
            for (Category category : categories) {
                if (category.getName().equals(excludedCategory)) {
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
