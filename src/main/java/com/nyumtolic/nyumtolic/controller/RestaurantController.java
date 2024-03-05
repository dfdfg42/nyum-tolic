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
    public String showRestaurantList(Model model) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants(); // 서비스 계층에서 맛집 리스트를 가져옴
        model.addAttribute("restaurants", restaurants); // 모델에 맛집 리스트 추가
        return "restaurant/list"; // 뷰 이름 반환
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
        return "restaurant/recommendation";
    }

    @GetMapping("/list/{categoryId}")
    public String showRestaurantsByCategory(@PathVariable Long categoryId, Model model) {
        //List<Restaurant> restaurants = restaurantService.findAllByCategoryId(categoryId);
        //model.addAttribute("restaurants", restaurants);
        return "restaurant/list"; // 맛집 리스트 페이지로 이동
    }
        

}
