package com.nyumtolic.nyumtolic.repository;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RestaurantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category koreanCategory;
    private Category chineseCategory;
    private Restaurant restaurant1;
    private Restaurant restaurant2;
    private Restaurant restaurant3;

    @BeforeEach
    void setUp() {
        // Create and persist categories
        koreanCategory = new Category();
        koreanCategory.setName("Korean");
        entityManager.persist(koreanCategory);

        chineseCategory = new Category();
        chineseCategory.setName("Chinese");
        entityManager.persist(chineseCategory);

        // Create and persist restaurants
        restaurant1 = new Restaurant();
        restaurant1.setName("Bibimbap House");
        restaurant1.setAddress("123 Main St");
        restaurant1.setPhoneNumber("123-456-7890");
        restaurant1.setRating(4.5);
        restaurant1.setUserRating(4.8);
        restaurant1.setDescription("Korean restaurant");
        restaurant1.setMenu(Arrays.asList("Bibimbap", "Bulgogi"));
        restaurant1.setCategories(new ArrayList<>(Arrays.asList(koreanCategory)));
        entityManager.persist(restaurant1);

        restaurant2 = new Restaurant();
        restaurant2.setName("Dragon Palace");
        restaurant2.setAddress("456 Elm St");
        restaurant2.setPhoneNumber("098-765-4321");
        restaurant2.setRating(4.2);
        restaurant2.setUserRating(4.0);
        restaurant2.setDescription("Chinese restaurant");
        restaurant2.setMenu(Arrays.asList("Kung Pao Chicken", "Dumplings"));
        restaurant2.setCategories(new ArrayList<>(Arrays.asList(chineseCategory)));
        entityManager.persist(restaurant2);

        restaurant3 = new Restaurant();
        restaurant3.setName("Seoul Kitchen");
        restaurant3.setAddress("789 Oak St");
        restaurant3.setPhoneNumber("111-222-3333");
        restaurant3.setRating(4.0);
        restaurant3.setUserRating(null); // No user rating
        restaurant3.setDescription("Another Korean restaurant");
        restaurant3.setMenu(Arrays.asList("Kimchi Jjigae", "Japchae"));
        restaurant3.setCategories(new ArrayList<>(Arrays.asList(koreanCategory)));
        entityManager.persist(restaurant3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Find all restaurants ordered by user rating")
    void findAllOrderByUserRating() {
        // Act
        List<Restaurant> restaurants = restaurantRepository.findAllOrderByUserRating();

        // Assert
        assertThat(restaurants).hasSize(3);
        assertThat(restaurants.get(0).getName()).isEqualTo("Bibimbap House"); // Highest user rating (4.8)
        assertThat(restaurants.get(1).getName()).isEqualTo("Dragon Palace"); // Second highest user rating (4.0)
        assertThat(restaurants.get(2).getName()).isEqualTo("Seoul Kitchen"); // Null user rating, should be last
    }

    @Test
    @DisplayName("Find all restaurants ordered by name")
    void findAllOrderByName() {
        // Act
        List<Restaurant> restaurants = restaurantRepository.findAllOrderByName();

        // Assert
        assertThat(restaurants).hasSize(3);
        assertThat(restaurants.get(0).getName()).isEqualTo("Bibimbap House"); // B comes before D and S
        assertThat(restaurants.get(1).getName()).isEqualTo("Dragon Palace");
        assertThat(restaurants.get(2).getName()).isEqualTo("Seoul Kitchen");
    }

    @Test
    @DisplayName("Find all restaurants by category ID")
    void findAllByCategoryId() {
        // Act
        List<Restaurant> koreanRestaurants = restaurantRepository.findAllByCategoryId(koreanCategory.getId());
        List<Restaurant> chineseRestaurants = restaurantRepository.findAllByCategoryId(chineseCategory.getId());

        // Assert
        assertThat(koreanRestaurants).hasSize(2);
        assertThat(koreanRestaurants).extracting("name").containsExactlyInAnyOrder("Bibimbap House", "Seoul Kitchen");

        assertThat(chineseRestaurants).hasSize(1);
        assertThat(chineseRestaurants).extracting("name").containsExactly("Dragon Palace");
    }

    @Test
    @DisplayName("Find all restaurants by category ID ordered by user rating")
    void findAllByCategoryIdOrderByUserRating() {
        // Act
        List<Restaurant> koreanRestaurants = restaurantRepository.findAllByCategoryIdOrderByUserRating(koreanCategory.getId());

        // Assert
        assertThat(koreanRestaurants).hasSize(2);
        assertThat(koreanRestaurants.get(0).getName()).isEqualTo("Bibimbap House"); // Higher user rating (4.8)
        assertThat(koreanRestaurants.get(1).getName()).isEqualTo("Seoul Kitchen"); // Null user rating, should be last
    }

    @Test
    @DisplayName("Find all restaurants by category ID ordered by name")
    void findAllByCategoryIdOrderByName() {
        // Act
        List<Restaurant> koreanRestaurants = restaurantRepository.findAllByCategoryIdOrderByName(koreanCategory.getId());

        // Assert
        assertThat(koreanRestaurants).hasSize(2);
        assertThat(koreanRestaurants.get(0).getName()).isEqualTo("Bibimbap House"); // B comes before S
        assertThat(koreanRestaurants.get(1).getName()).isEqualTo("Seoul Kitchen");
    }

    @Test
    @DisplayName("Find restaurant by name")
    void findByName() {
        // Act
        Optional<Restaurant> result = restaurantRepository.findByName("Bibimbap House");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bibimbap House");
        assertThat(result.get().getDescription()).isEqualTo("Korean restaurant");
    }

    @Test
    @DisplayName("Find restaurant by name - not found")
    void findByNameNotFound() {
        // Act
        Optional<Restaurant> result = restaurantRepository.findByName("Nonexistent Restaurant");

        // Assert
        assertThat(result).isEmpty();
    }
}