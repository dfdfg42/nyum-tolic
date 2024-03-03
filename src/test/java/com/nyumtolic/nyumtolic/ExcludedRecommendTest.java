package com.nyumtolic.nyumtolic;


import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ExcludedRecommendTest {

    @Autowired
    RestaurantService restaurantService;
    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void test(){

        //given

        Category category1 = new Category();
        category1.setName("중식");
        Category category2 = new Category();
        category2.setName("양식");
        Category category3 = new Category();
        category3.setName("면류");
        Category category4 = new Category();
        category4.setName("밥류");
        Category category5 = new Category();
        category5.setName("한식");

        List<Category>categories1 = new ArrayList<>(); // 중 면
        categories1.add(category1);
        categories1.add(category3);
        List<Category>categories2 = new ArrayList<>(); // 한 밥
        categories2.add(category5);
        categories2.add(category4);
        List<Category>categories3 = new ArrayList<>(); // 한 면
        categories3.add(category5);
        categories3.add(category3);
        List<Category>categories4 = new ArrayList<>(); // 한 면
        categories4.add(category5);

        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("북경");
        restaurant1.setCategories(categories1);
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("꼬밥");
        restaurant2.setCategories(categories2);
        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("삼복");
        restaurant3.setCategories(categories4);
        Restaurant restaurant4 = new Restaurant();
        restaurant4.setName("메밀꽃");
        restaurant4.setCategories(categories2);

        //when
        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);
        categoryRepository.save(category4);
        categoryRepository.save(category5);


        restaurantService.save(restaurant1);
        restaurantService.save(restaurant2);
        restaurantService.save(restaurant3);
        restaurantService.save(restaurant4);




    }

}
