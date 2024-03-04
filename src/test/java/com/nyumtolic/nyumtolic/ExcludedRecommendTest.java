package com.nyumtolic.nyumtolic;


import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ExcludedRecommendTest {

    @Autowired
    RestaurantService restaurantService;
    @Autowired
    RestaurantRepository restaurantRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @AfterEach
    public void cleanup() {
        //restaurantRepository.deleteAll();
        //categoryRepository.deleteAll();
    }

    @Test
    void recommendRestaurantExcludingSpecificCategories() {
        // given: 카테고리와 레스토랑을 설정
        setupTestData();

        // when: 특정 카테고리(예: "양식")를 제외하고 레스토랑 추천
        Optional<Restaurant> recommendedRestaurant = restaurantService.recommendRandomRestaurantExcludingCategories("양식");

        // then: 추천된 레스토랑은 "양식" 카테고리를 포함하지 않아야 함
        Assertions.assertTrue(recommendedRestaurant.isPresent());
        Assertions.assertFalse(recommendedRestaurant.get().getCategories().stream()
                        .anyMatch(category -> category.getName().equals("양식")),
                "추천된 레스토랑은 '양식' 카테고리를 포함하지 않아야 합니다.");

        Restaurant restaurant = recommendedRestaurant.get();
        List<Category> categories = restaurant.getCategories();
        for (Category c : categories){
            System.out.println("c.getName() = " + c.getName());
        }
    }

    private void setupTestData() {
        Category chinese = new Category();
        chinese.setName("중식");
        categoryRepository.save(chinese);

        Category western = new Category();
        western.setName("양식");
        categoryRepository.save(western);

        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("북경");
        restaurant1.setCategories(Arrays.asList(chinese));
        restaurantService.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("피자헛");
        restaurant2.setCategories(Arrays.asList(western));
        restaurantService.save(restaurant2);
    }



}

