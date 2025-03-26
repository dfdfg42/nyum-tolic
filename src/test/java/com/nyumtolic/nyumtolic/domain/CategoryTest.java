package com.nyumtolic.nyumtolic.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    private Category category;
    private Restaurant restaurant1;
    private Restaurant restaurant2;

    @BeforeEach
    void setUp() {
        category = new Category();
        
        restaurant1 = new Restaurant();
        restaurant1.setId(1L);
        restaurant1.setName("Restaurant 1");
        
        restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Restaurant 2");
    }

    @Test
    @DisplayName("Test Category entity properties")
    void testCategoryProperties() {
        // Set properties
        category.setId(1L);
        category.setName("Test Category");
        category.setMainCategory(true);
        
        // Verify properties
        assertEquals(1L, category.getId());
        assertEquals("Test Category", category.getName());
        assertTrue(category.isMainCategory());
    }

    @Test
    @DisplayName("Test Category-Restaurant relationship")
    void testCategoryRestaurantRelationship() {
        // Set up bidirectional relationship
        List<Restaurant> restaurants = Arrays.asList(restaurant1, restaurant2);
        
        // Each restaurant has categories (one-to-many)
        restaurant1.setCategories(Arrays.asList(category));
        restaurant2.setCategories(Arrays.asList(category));
        
        // Set the restaurants for the category (many-to-many)
        category.setRestaurants(restaurants);
        
        // Verify relationships
        assertThat(category.getRestaurants()).hasSize(2);
        assertThat(category.getRestaurants()).containsExactly(restaurant1, restaurant2);
        
        // Verify bidirectional mapping
        assertThat(restaurant1.getCategories()).hasSize(1);
        assertThat(restaurant1.getCategories().get(0)).isEqualTo(category);
        
        assertThat(restaurant2.getCategories()).hasSize(1);
        assertThat(restaurant2.getCategories().get(0)).isEqualTo(category);
    }

    @Test
    @DisplayName("Test main category flag")
    void testMainCategoryFlag() {
        // Default should be false
        assertFalse(category.isMainCategory());
        
        // Set to true
        category.setMainCategory(true);
        assertTrue(category.isMainCategory());
        
        // Set back to false
        category.setMainCategory(false);
        assertFalse(category.isMainCategory());
    }

    @Test
    @DisplayName("Test empty restaurants list")
    void testEmptyRestaurantsList() {
        // Initially null
        assertNull(category.getRestaurants());
        
        // Set empty list
        category.setRestaurants(new ArrayList<>());
        assertNotNull(category.getRestaurants());
        assertTrue(category.getRestaurants().isEmpty());
    }
}