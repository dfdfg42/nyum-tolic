package com.nyumtolic.nyumtolic.integration;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RestaurantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setMainCategory(true);
        categoryRepository.save(testCategory);

        // Create test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setPhoneNumber("123-456-7890");
        testRestaurant.setRating(4.5);
        testRestaurant.setUserRating(4.2);
        testRestaurant.setPhoto("https://test-photo.jpg");
        testRestaurant.setDescription("Test restaurant description");
        testRestaurant.setTravelTime(10);
        testRestaurant.setMenu(Arrays.asList("Dish 1", "Dish 2"));
        testRestaurant.setCategories(new ArrayList<>(Arrays.asList(testCategory)));
        restaurantRepository.save(testRestaurant);
    }

    @Test
    @DisplayName("Restaurant list page loads and shows restaurant")
    void restaurantListPageLoadsAndShowsRestaurant() throws Exception {
        mockMvc.perform(get("/restaurant/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/list"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(content().string(containsString("Test Restaurant")));
    }

    @Test
    @DisplayName("Restaurant detail page loads correctly")
    void restaurantDetailPageLoads() throws Exception {
        mockMvc.perform(get("/restaurant/detail/" + testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/detail"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("menuString"))
                .andExpect(model().attributeExists("categoryString"))
                .andExpect(content().string(containsString("Test Restaurant")))
                .andExpect(content().string(containsString("Test restaurant description")))
                .andExpect(content().string(containsString("Dish 1, Dish 2")));
    }

    @Test
    @DisplayName("Restaurant recommendation page loads")
    void restaurantRecommendationPageLoads() throws Exception {
        mockMvc.perform(get("/restaurant/recommendation"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/recommendation"))
                .andExpect(model().attributeExists("excludedCategories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin can access restaurant admin list")
    void adminCanAccessRestaurantAdminList() throws Exception {
        mockMvc.perform(get("/restaurant/admin/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/admin/restaurant_admin_list"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(content().string(containsString("Test Restaurant")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Regular user cannot access admin pages")
    void regularUserCannotAccessAdminPages() throws Exception {
        mockMvc.perform(get("/restaurant/admin/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Filter restaurants by category")
    void filterRestaurantsByCategory() throws Exception {
        mockMvc.perform(get("/restaurant/list").param("categoryId", testCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/list"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(model().attribute("categoryId", testCategory.getId()))
                .andExpect(content().string(containsString("Test Restaurant")));
    }

    @Test
    @DisplayName("Sort restaurants by user rating")
    void sortRestaurantsByUserRating() throws Exception {
        // Create another restaurant with different rating
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Another Restaurant");
        restaurant2.setAddress("456 Test Ave");
        restaurant2.setUserRating(3.8); // Lower rating
        restaurant2.setCategories(new ArrayList<>(Arrays.asList(testCategory)));
        restaurantRepository.save(restaurant2);

        mockMvc.perform(get("/restaurant/list").param("sort", "userRating"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/list"))
                .andExpect(model().attributeExists("restaurants"));
                
        // Our restaurants should be sorted by user rating (highest first)
        List<Restaurant> sortedRestaurants = restaurantService.getAllRestaurantsBySorted("userRating");
        assertThat(sortedRestaurants).isNotEmpty();
        assertThat(sortedRestaurants.get(0).getUserRating()).isGreaterThanOrEqualTo(sortedRestaurants.get(1).getUserRating());
    }

    @Test
    @DisplayName("Restaurant service retrieves restaurant by ID")
    void restaurantServiceRetrievesById() {
        Optional<Restaurant> restaurant = restaurantService.getRestaurantsById(testRestaurant.getId());
        
        assertThat(restaurant).isPresent();
        assertThat(restaurant.get().getName()).isEqualTo("Test Restaurant");
        assertThat(restaurant.get().getUserRating()).isEqualTo(4.2);
    }
}