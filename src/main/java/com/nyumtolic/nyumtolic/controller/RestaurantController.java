package com.nyumtolic.nyumtolic.controller;


import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/list")
    public String showRestaurantList(@RequestParam(value = "categoryId", required = false) Long categoryId, Model model) {
        List<Restaurant> restaurants;
        if (categoryId != null) {
            // 특정 카테고리 ID가 제공된 경우, 해당 카테고리의 맛집 리스트를 가져옵니다.
            restaurants = restaurantService.findAllByCategoryId(categoryId);
        } else {
            // 카테고리 ID가 제공되지 않은 경우, 전체 맛집 리스트를 가져옵니다.
            restaurants = restaurantService.getAllRestaurants();
        }
        model.addAttribute("restaurants", restaurants);
        return "restaurant/list"; // 맛집 리스트 페이지로 이동
    }

    @GetMapping("/recommendation")
    public String showRecommendationForm(Model model) {
        model.addAttribute("recommendedRestaurant", null);
        return "restaurant/recommendation";
    }

    @PostMapping("/recommend")
    public String recommendRestaurant(@RequestParam("excludedCategories") String excludedCategories, Model model) {
        // 사용자 입력을 쉼표로 분리하여 배열로 변환
        String[] categoriesArray = excludedCategories.split("\\s*,\\s*");
        Optional<Restaurant> recommendedRestaurant = restaurantService.recommendRandomRestaurantExcludingCategories(categoriesArray);
        model.addAttribute("recommendedRestaurant", recommendedRestaurant.orElse(null));
        // 사용자가 선택한 카테고리를 모델에 추가
        model.addAttribute("excludedCategories", excludedCategories);
        return "restaurant/recommendation";
    }

        

}
