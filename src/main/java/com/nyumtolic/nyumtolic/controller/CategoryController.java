package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService; // 카테고리 서비스를 주입받음

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.findAllCategories(); // 모든 카테고리 조회
    }
}
