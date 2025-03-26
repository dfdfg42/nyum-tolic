package com.nyumtolic.nyumtolic;

import com.nyumtolic.nyumtolic.controller.MainController;
import com.nyumtolic.nyumtolic.controller.RestaurantController;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
import com.nyumtolic.nyumtolic.review.ReviewRepository;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NyumtolicApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MainController mainController;

    @Autowired
    private RestaurantController restaurantController;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLogRepository reviewLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        // Verify controllers loaded successfully
        assertThat(mainController).isNotNull();
        assertThat(restaurantController).isNotNull();
        
        // Verify services loaded successfully
        assertThat(restaurantService).isNotNull();
        
        // Verify repositories loaded successfully
        assertThat(restaurantRepository).isNotNull();
        assertThat(categoryRepository).isNotNull();
        assertThat(reviewRepository).isNotNull();
        assertThat(reviewLogRepository).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @DisplayName("Home page loads successfully")
    void homePageLoads() {
        // Act
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
        
        // Assert
        assertThat(response).isNotNull();
        // Check for something we expect on the home page
        assertThat(response).contains("</html>");
    }

    @Test
    @DisplayName("Restaurant list page loads successfully")
    void restaurantListPageLoads() {
        // Act
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/restaurant/list", String.class);
        
        // Assert
        assertThat(response).isNotNull();
        // Check for something we expect on the restaurant list page
        assertThat(response).contains("</html>");
    }

    @Test
    @DisplayName("Restaurant recommendation page loads successfully")
    void restaurantRecommendationPageLoads() {
        // Act
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/restaurant/recommendation", String.class);
        
        // Assert
        assertThat(response).isNotNull();
        // Check for something we expect on the recommendation page
        assertThat(response).contains("</html>");
    }

    @Test
    @DisplayName("Login page loads successfully")
    void loginPageLoads() {
        // Act
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/user/login", String.class);
        
        // Assert
        assertThat(response).isNotNull();
        // Check for something we expect on the login page
        assertThat(response).contains("</html>");
    }
}