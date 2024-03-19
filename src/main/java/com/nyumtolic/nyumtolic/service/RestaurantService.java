package com.nyumtolic.nyumtolic.service;


import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;


    // 저장
    public void save(Restaurant restaurant){
        restaurantRepository.save(restaurant);
    }



    // 전체 Restaurant 리스트를 반환하는 메서드
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // 특정 id를 가진 Restaurant 반환하는 메서드
    public Optional<Restaurant> getRestaurantsById(Long id){
        return restaurantRepository.findById(id);
    }

    //특정 카테고리를 가진 Restaurant를 카테고리id 로 조회해서 반환
    public List<Restaurant> findAllByCategoryId(Long categoryId) {
        return restaurantRepository.findAllByCategoryId(categoryId);
    }

    // 특정 카테고리 제외, 나머지 음식점 추천 하는 메서드
    public Optional<Restaurant> recommendRandomRestaurantExcludingCategories(String ... excludedCategories ){
        List<Restaurant> allRestaurants = getAllRestaurants();
        if (allRestaurants.isEmpty()){
            return Optional.empty();
        }

        // 필터링 해서 나머지 레스토랑 리스트 생성
        List<Restaurant> filteredRestaurants = allRestaurants.stream()
                .filter(restaurant -> !isExcluded(restaurant.getCategories(),excludedCategories))
                .collect(Collectors.toList());

        // 리스트가 empty 인지 확인
        if (filteredRestaurants.isEmpty()){
            return Optional.empty();
        }

        // 난수 생성 후 음식점 추천
        Random random = new Random();
        int randomIdx = random.nextInt(filteredRestaurants.size());
        return Optional.of(filteredRestaurants.get(randomIdx));

    }

    // 카테고리 목록에 제외할 카테고리가 있는지 확인
    private boolean isExcluded(List<Category> categories, String[] excludedCategories){
        for (String excludedCategory:excludedCategories){
            for (Category category : categories){
                if (category.getName().equals(excludedCategory)){
                    return true;
                }
            }
        }
        return false;
    }


}
