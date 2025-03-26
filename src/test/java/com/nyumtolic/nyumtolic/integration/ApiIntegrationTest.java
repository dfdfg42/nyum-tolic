package com.nyumtolic.nyumtolic.integration;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewLogRepository reviewLogRepository;

    private Restaurant testRestaurant;
    private Category testCategory;
    private SiteUser testUser;
    private ReviewLog testReviewLog;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("API Test Category");
        testCategory.setMainCategory(true);
        categoryRepository.save(testCategory);

        // Create test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setName("API Test Restaurant");
        testRestaurant.setAddress("123 API Test St");
        testRestaurant.setPhoneNumber("123-456-7890");
        testRestaurant.setRating(4.5);
        testRestaurant.setUserRating(4.2);
        testRestaurant.setPhoto("https://api-test-photo.jpg");
        testRestaurant.setDescription("API test restaurant description");
        testRestaurant.setTravelTime(10);
        testRestaurant.setMenu(Arrays.asList("API Dish 1", "API Dish 2"));
        testRestaurant.setCategories(new ArrayList<>(Arrays.asList(testCategory)));
        restaurantRepository.save(testRestaurant);

        // Create test user
        testUser = SiteUser.builder()
                .loginId("apitest@example.com")
                .nickname("API Test User")
                .email("apitest@example.com")
                .password("password")
                .enabled(true)
                .role(UserRole.USER)
                .build();
        userRepository.save(testUser);

        // Create test review log
        testReviewLog = new ReviewLog();
        testReviewLog.setCreatedAt(LocalDateTime.now());
        testReviewLog.setContent("API test review content");
        testReviewLog.setRating(4.5);
        testReviewLog.setAuthor(testUser);
        testReviewLog.setRestaurantId(testRestaurant.getId());
        reviewLogRepository.save(testReviewLog);
    }

    @Test
    @DisplayName("API endpoints are commented out but should not throw 500 errors")
    void apiEndpointsAreCommentedOutButShouldNotThrow500Errors() throws Exception {
        // All these API endpoints are commented out in the code, but we want to verify that
        // they return 404 (not found) instead of 500 (server error)
        
        mockMvc.perform(get("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/restaurants/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/review-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/review-logs/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin API endpoints return 404 when commented out")
    void adminApiEndpointsReturnNotFoundWhenCommentedOut() throws Exception {
        mockMvc.perform(get("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // The following tests would work if the API endpoints were uncommented in the code:
    
    /*
    @Test
    @DisplayName("Get all restaurants API endpoint returns correct data")
    void getAllRestaurantsApiEndpointReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/restaurants/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("API Test Restaurant"))
                .andExpect(jsonPath("$[0].address").value("123 API Test St"));
    }

    @Test
    @DisplayName("Get paginated restaurants API endpoint returns correct data")
    void getPaginatedRestaurantsApiEndpointReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(isA(Number.class)));
    }

    @Test
    @DisplayName("Get all users API endpoint returns correct data")
    void getAllUsersApiEndpointReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nickname").value("API Test User"))
                .andExpect(jsonPath("$[0].loginId").value("apitest@example.com"));
    }

    @Test
    @DisplayName("Get all review logs API endpoint returns correct data")
    void getAllReviewLogsApiEndpointReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/review-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("API test review content"))
                .andExpect(jsonPath("$[0].rating").value(4.5));
    }

    @Test
    @DisplayName("Get review logs by user API endpoint returns correct data")
    void getReviewLogsByUserApiEndpointReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/review-logs/user/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("API test review content"))
                .andExpect(jsonPath("$[0].rating").value(4.5));
    }
    */
}