package com.nyumtolic.nyumtolic.controller;


import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/")
    public RedirectView redirectToRestaurantList() {
        return new RedirectView("/restaurant/list");
    }

    @GetMapping("/restaurant/list")
    public String showRestaurantList(Model model) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants(); // 서비스 계층에서 맛집 리스트를 가져옴
        model.addAttribute("restaurants", restaurants); // 모델에 맛집 리스트 추가
        return "restaurant/list"; // 뷰 이름 반환
    }
}
