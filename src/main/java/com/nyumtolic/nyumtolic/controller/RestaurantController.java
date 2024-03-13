package com.nyumtolic.nyumtolic.controller;


import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import groovy.util.logging.Log4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);


    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id) {
        this.restaurantService.getRestaurantsById(id).ifPresent(restaurant -> model.addAttribute("restaurant", restaurant));
        Optional<Restaurant> restaurantsById = restaurantService.getRestaurantsById(id);
        if (restaurantsById.isPresent()){
            String manuString = String.join(", ", restaurantsById.get().getMenu());
            model.addAttribute("menuString", manuString);
            List<String> catagoryNames= new ArrayList<>();
            for (Category category: restaurantsById.get().getCategories()){
                catagoryNames.add(category.getName());
            }
            String categoryString = String.join(", ", catagoryNames);
            model.addAttribute("categoryString", categoryString);
        }
        return "restaurant/detail";

    }

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

        if (recommendedRestaurant.isPresent()){
            String manuString = String.join(", ", recommendedRestaurant.get().getMenu());
            model.addAttribute("menuString", manuString);
            List<String> catagoryNames= new ArrayList<>();
            for (Category category: recommendedRestaurant.get().getCategories()){
                catagoryNames.add(category.getName());
            }
            String categoryString = String.join(", ", catagoryNames);
            model.addAttribute("categoryString", categoryString);
        }

        model.addAttribute("recommendedRestaurant", recommendedRestaurant.orElse(null));
        // 사용자가 선택한 카테고리를 모델에 추가
        model.addAttribute("excludedCategories", excludedCategories);
        return "restaurant/recommendation";
    }

        

}
