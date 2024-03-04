package com.nyumtolic.nyumtolic.config;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository, RestaurantService restaurantService) {
        return args -> {
            // 더미 카테고리 데이터 생성 및 저장
            Category chinese = new Category();
            chinese.setName("중식");
            categoryRepository.save(chinese);

            Category western = new Category();
            western.setName("양식");
            categoryRepository.save(western);

            Category korean = new Category();
            korean.setName("한식");
            categoryRepository.save(korean);

            Category noodle = new Category();
            noodle.setName("면류");
            categoryRepository.save(noodle);

            // 더미 레스토랑 데이터 생성 및 저장
            Restaurant restaurant1 = new Restaurant();
            restaurant1.setName("북경");
            restaurant1.setCategories(Arrays.asList(chinese,noodle));
            restaurantService.save(restaurant1);

            Restaurant restaurant2 = new Restaurant();
            restaurant2.setName("피자헛");
            restaurant2.setCategories(Arrays.asList(western));
            restaurantService.save(restaurant2);

            Restaurant restaurant3 = new Restaurant();
            restaurant3.setName("아웃벡");
            restaurant3.setCategories(Arrays.asList(western));
            restaurantService.save(restaurant3);

            Restaurant restaurant4 = new Restaurant();
            restaurant4.setName("메밀꽃");
            restaurant4.setCategories(Arrays.asList(korean));
            restaurantService.save(restaurant4);
        };
    }
}