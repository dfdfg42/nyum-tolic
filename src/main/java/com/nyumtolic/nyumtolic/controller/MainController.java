package com.nyumtolic.nyumtolic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class MainController {

    @GetMapping("/")
    public String redirectToRestaurantList() {
        return "redirect:/restaurant/list";
    }


}
