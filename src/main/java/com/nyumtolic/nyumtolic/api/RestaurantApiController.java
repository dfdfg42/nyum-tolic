package com.nyumtolic.nyumtolic.api;


import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestaurantApiController {

    private final RestaurantService restaurantService;

    @GetMapping("/restaurants")
    public List<RestaurantDTO> getCreateAPI() {
        return restaurantService.getAllRestaurantsDTO();
    }

}
