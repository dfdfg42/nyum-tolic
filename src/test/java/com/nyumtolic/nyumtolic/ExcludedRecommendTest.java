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
        restaurantRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void recommendRestaurantExcludingSpecificCategories() {
        // given: 카테고리와 레스토랑을 설정
        setupTestData();

   /*     // when: 특정 카테고리(예: "양식")를 제외하고 레스토랑 추천
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
        }*/
    }

    private void setupTestData() {
        // 메인 카테고리 생성 및 저장
        Category chinese = new Category();
        chinese.setName("중식");
        chinese.setMainCategory(true); // 메인 카테고리로 설정
        categoryRepository.save(chinese);

        Category western = new Category();
        western.setName("양식");
        western.setMainCategory(true); // 메인 카테고리로 설정
        categoryRepository.save(western);

        // 기타 카테고리 생성 및 저장
        Category dessert = new Category();
        dessert.setName("디저트");
        dessert.setMainCategory(false); // 메인 카테고리가 아님
        categoryRepository.save(dessert);

        Category korean = new Category();
        korean.setName("한식");
        korean.setMainCategory(false); // 메인 카테고리가 아님
        categoryRepository.save(korean);

        // 레스토랑 생성 및 카테고리 할당
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("북경");
        restaurant1.setCategories(Arrays.asList(chinese)); // 중식 카테고리 할당
        restaurantService.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("피자헛");
        restaurant2.setCategories(Arrays.asList(western)); // 양식 카테고리 할당
        restaurantService.save(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("스위트홈");
        restaurant3.setCategories(Arrays.asList(dessert)); // 디저트 카테고리 할당
        restaurantService.save(restaurant3);

        Restaurant restaurant4 = new Restaurant();
        restaurant4.setName("한가람");
        restaurant4.setCategories(Arrays.asList(korean)); // 한식 카테고리 할당
        restaurantService.save(restaurant4);
    }



}

