package com.nyumtolic.nyumtolic.api.controller;


import com.nyumtolic.nyumtolic.api.domain.PageResponse;
import com.nyumtolic.nyumtolic.api.domain.RestaurantDTO;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestaurantApiController {

    private final RestaurantService restaurantService;

    @GetMapping("/restaurants")
    public PageResponse<RestaurantDTO> getCreateAPI(@PageableDefault(size = 5) Pageable pageable) {
        return restaurantService.getAllRestaurantsDTO(pageable);
    }

}