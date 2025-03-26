package com.nyumtolic.nyumtolic.domain;

import com.nyumtolic.nyumtolic.review.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {

    private Restaurant restaurant;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Korean");
        
        category2 = new Category();
        category2.setId(2L);
        category2.setName("Chinese");
    }

    @Test
    @DisplayName("Test Restaurant entity properties")
    void testRestaurantProperties() {
        // Set properties
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("123 Test St");
        restaurant.setPhoneNumber("123-456-7890");
        restaurant.setPhoto("https://test-photo.jpg");
        restaurant.setRating(4.5);
        restaurant.setUserRating(4.2);
        restaurant.setDescription("Test description");
        restaurant.setTravelTime(10);
        restaurant.setLatitude(37.123);
        restaurant.setLongitude(127.456);
        
        List<String> menu = Arrays.asList("Dish 1", "Dish 2", "Dish 3");
        restaurant.setMenu(menu);
        
        List<Category> categories = Arrays.asList(category1, category2);
        restaurant.setCategories(categories);
        
        // Verify properties
        assertEquals(1L, restaurant.getId());
        assertEquals("Test Restaurant", restaurant.getName());
        assertEquals("123 Test St", restaurant.getAddress());
        assertEquals("123-456-7890", restaurant.getPhoneNumber());
        assertEquals("https://test-photo.jpg", restaurant.getPhoto());
        assertEquals(4.5, restaurant.getRating());
        assertEquals(4.2, restaurant.getUserRating());
        assertEquals("Test description", restaurant.getDescription());
        assertEquals(10, restaurant.getTravelTime());
        assertEquals(37.123, restaurant.getLatitude());
        assertEquals(127.456, restaurant.getLongitude());
        
        assertThat(restaurant.getMenu()).hasSize(3);
        assertThat(restaurant.getMenu()).containsExactly("Dish 1", "Dish 2", "Dish 3");
        
        assertThat(restaurant.getCategories()).hasSize(2);
        assertThat(restaurant.getCategories()).containsExactly(category1, category2);
    }

    @Test
    @DisplayName("Test Restaurant reviews association")
    void testRestaurantReviews() {
        // Create and add reviews
        Review review1 = new Review();
        review1.setId(1);
        review1.setContent("Great food!");
        review1.setRating(5.0);
        review1.setRestaurant(restaurant);
        
        Review review2 = new Review();
        review2.setId(2);
        review2.setContent("Good service!");
        review2.setRating(4.0);
        review2.setRestaurant(restaurant);
        
        List<Review> reviews = new ArrayList<>();
        reviews.add(review1);
        reviews.add(review2);
        restaurant.setReviews(reviews);
        
        // Verify reviews
        assertThat(restaurant.getReviews()).hasSize(2);
        assertThat(restaurant.getReviews()).containsExactly(review1, review2);
        
        // Verify bidirectional relationship
        assertEquals(restaurant, review1.getRestaurant());
        assertEquals(restaurant, review2.getRestaurant());
    }
}