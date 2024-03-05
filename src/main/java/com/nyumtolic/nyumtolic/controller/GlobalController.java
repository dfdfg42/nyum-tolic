package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {


    private final CategoryService categoryService;


    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.findAllCategories();

    }

    @ModelAttribute("mainCategories")
    public List<Category> mainCategories() {
        return categoryService.findMainCategories();
    }

    @ModelAttribute("otherCategories")
    public List<Category> otherCategories() {
        return categoryService.findOtherCategories();
    }
}
