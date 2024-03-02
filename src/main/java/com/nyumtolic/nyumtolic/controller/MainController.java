package com.nyumtolic.nyumtolic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MainController {

    @GetMapping("/")
    public String redirectToRestaurantList() {
        return "redirect:/restaurant/list";
    }

}
