package com.nyumtolic.nyumtolic.util;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.review.Review;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for creating test fixtures for unit tests.
 */
public class TestUtils {
    
    /**
     * Creates a sample restaurant for testing.
     *
     * @param id The ID to assign to the restaurant
     * @param name The name of the restaurant
     * @return A Restaurant object with sample data
     */
    public static Restaurant createSampleRestaurant(Long id, String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName(name);
        restaurant.setAddress(name + " Address");
        restaurant.setPhoneNumber("123-456-" + id);
        restaurant.setRating(4.5);
        restaurant.setUserRating(4.0);
        restaurant.setPhoto("https://test-photo-" + id + ".jpg");
        restaurant.setDescription("Description for " + name);
        restaurant.setTravelTime(10);
        restaurant.setLatitude(37.123 + (id * 0.1));
        restaurant.setLongitude(127.456 + (id * 0.1));
        restaurant.setMenu(Arrays.asList("Menu 1", "Menu 2"));
        restaurant.setCategories(new ArrayList<>());
        restaurant.setReviews(new ArrayList<>());
        
        return restaurant;
    }
    
    /**
     * Creates a sample category for testing.
     *
     * @param id The ID to assign to the category
     * @param name The name of the category
     * @param isMain Whether this is a main category
     * @return A Category object with sample data
     */
    public static Category createSampleCategory(Long id, String name, boolean isMain) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setMainCategory(isMain);
        category.setRestaurants(new ArrayList<>());
        
        return category;
    }
    
    /**
     * Creates a sample user for testing.
     *
     * @param id The ID to assign to the user
     * @param username The username
     * @param role The role of the user
     * @return A SiteUser object with sample data
     */
    public static SiteUser createSampleUser(Long id, String username, UserRole role) {
        SiteUser user = new SiteUser();
        user.setId(id);
        user.setNickname(username + " Nickname");
        user.setEmail(username + "@example.com");
        user.setLoginId(username + "@example.com");
        user.setPassword("password123");
        user.setRole(role);
        
        return user;
    }
    
    /**
     * Creates a sample review for testing.
     *
     * @param id The ID to assign to the review
     * @param content The content of the review
     * @param rating The rating given in the review
     * @param restaurant The restaurant being reviewed
     * @param author The author of the review
     * @return A Review object with sample data
     */
    public static Review createSampleReview(Long id, String content, Double rating, Restaurant restaurant, SiteUser author) {
        Review review = new Review();
        review.setId(id.intValue());
        review.setContent(content);
        review.setRating(rating);
        review.setCreateDate(LocalDateTime.now());
        review.setRestaurant(restaurant);
        review.setAuthor(author);
        review.setVoter(new HashSet<>());
        
        return review;
    }
    
    /**
     * Adds a category to a restaurant (and vice versa).
     *
     * @param restaurant The restaurant to update
     * @param category The category to add
     */
    public static void addCategoryToRestaurant(Restaurant restaurant, Category category) {
        if (restaurant.getCategories() == null) {
            restaurant.setCategories(new ArrayList<>());
        }
        restaurant.getCategories().add(category);
        
        if (category.getRestaurants() == null) {
            category.setRestaurants(new ArrayList<>());
        }
        category.getRestaurants().add(restaurant);
    }
    
    /**
     * Adds a review to a restaurant.
     *
     * @param restaurant The restaurant to update
     * @param review The review to add
     */
    public static void addReviewToRestaurant(Restaurant restaurant, Review review) {
        if (restaurant.getReviews() == null) {
            restaurant.setReviews(new ArrayList<>());
        }
        restaurant.getReviews().add(review);
        review.setRestaurant(restaurant);
    }
}