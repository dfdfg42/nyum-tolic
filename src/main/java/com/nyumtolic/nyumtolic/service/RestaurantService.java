package com.nyumtolic.nyumtolic.service;

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

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

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

    // 특정 카테고리 제외, 나머지 음식점 추천하는 메서드
    public Optional<Restaurant> recommendRandomRestaurantExcludingCategories(String... excludedCategories) {
        List<Restaurant> allRestaurants = getAllRestaurants();
        if (allRestaurants.isEmpty()) {
            return Optional.empty();
        }

        // 필터링해서 나머지 레스토랑 리스트 생성
        List<Restaurant> filteredRestaurants = allRestaurants.stream()
                .filter(restaurant -> !isExcluded(restaurant.getCategories(), excludedCategories))
                .collect(Collectors.toList());

        // 리스트가 empty 인지 확인
        if (filteredRestaurants.isEmpty()) {
            return Optional.empty();
        }

        // 난수 생성 후 음식점 추천
        Random random = new Random();
        int randomIdx = random.nextInt(filteredRestaurants.size());
        return Optional.of(filteredRestaurants.get(randomIdx));
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
