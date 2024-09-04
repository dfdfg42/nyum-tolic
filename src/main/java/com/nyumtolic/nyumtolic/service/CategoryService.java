package com.nyumtolic.nyumtolic.service;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

<<<<<<< HEAD

    //저장
    public void save(Category category){
=======
    // 저장
    public void save(Category category) {
>>>>>>> s3ver
        categoryRepository.save(category);
    }

    // 모든 카테고리를 조회하는 메서드
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    // 카테고리 ID로 특정 카테고리를 조회하는 메서드
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    // 메인 카테고리 조회
    public List<Category> findMainCategories() {
        return categoryRepository.findByIsMainCategoryTrue();
    }

<<<<<<< HEAD
=======
    // 서브 카테고리 조회
>>>>>>> s3ver
    public List<Category> findOtherCategories() {
        return categoryRepository.findByIsMainCategoryFalse();
    }

<<<<<<< HEAD
    public Category findByName(String name){
        return categoryRepository.findByName(name).get();
    }



=======
    // 카테고리 이름으로 조회
    public Category findByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }

    // 모든 카테고리를 조회
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    // 카테고리 삭제
    public void delete(Category category) {
        categoryRepository.delete(category);
    }
>>>>>>> s3ver
}
