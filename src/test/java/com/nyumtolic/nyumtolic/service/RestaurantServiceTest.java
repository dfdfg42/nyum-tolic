package com.nyumtolic.nyumtolic.service;

import com.nyumtolic.nyumtolic.api.domain.PageResponse;
import com.nyumtolic.nyumtolic.api.domain.RestaurantDTO;
import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.review.ReviewRepository;
import com.nyumtolic.nyumtolic.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private List<Restaurant> testRestaurants;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Korean");

        List<Category> categories = new ArrayList<>();
        categories.add(testCategory);

        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("Test Address");
        testRestaurant.setPhoneNumber("123-456-7890");
        testRestaurant.setRating(4.5);
        testRestaurant.setUserRating(4.2);
        testRestaurant.setPhoto("https://test-photo-url.jpg");
        testRestaurant.setCategories(categories);
        testRestaurant.setMenu(Arrays.asList("Menu Item 1", "Menu Item 2"));
        testRestaurant.setDescription("Test Description");
        testRestaurant.setTravelTime(10);
        testRestaurant.setLatitude(37.123);
        testRestaurant.setLongitude(127.456);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Another Restaurant");
        restaurant2.setCategories(categories);
        restaurant2.setUserRating(4.0);

        testRestaurants = Arrays.asList(testRestaurant, restaurant2);
    }

    @Test
    @DisplayName("Save restaurant successfully")
    void save() {
        // Arrange
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // Act
        restaurantService.save(testRestaurant);

        // Assert
        verify(restaurantRepository, times(1)).save(testRestaurant);
    }

    @Test
    @DisplayName("Get all restaurants")
    void getAllRestaurants() {
        // Arrange
        when(restaurantRepository.findAll(any(Sort.class))).thenReturn(testRestaurants);

        // Act
        List<Restaurant> result = restaurantService.getAllRestaurants();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testRestaurant);
        verify(restaurantRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Get all restaurants sorted by user rating")
    void getAllRestaurantsBySortedUserRating() {
        // Arrange
        when(restaurantRepository.findAllOrderByUserRating()).thenReturn(testRestaurants);

        // Act
        List<Restaurant> result = restaurantService.getAllRestaurantsBySorted("userRating");

        // Assert
        assertThat(result).hasSize(2);
        verify(restaurantRepository, times(1)).findAllOrderByUserRating();
    }

    @Test
    @DisplayName("Get all restaurants sorted by name")
    void getAllRestaurantsBySortedName() {
        // Arrange
        when(restaurantRepository.findAllOrderByName()).thenReturn(testRestaurants);

        // Act
        List<Restaurant> result = restaurantService.getAllRestaurantsBySorted("name");

        // Assert
        assertThat(result).hasSize(2);
        verify(restaurantRepository, times(1)).findAllOrderByName();
    }

    @Test
    @DisplayName("Get restaurants by category ID sorted by user rating")
    void getAllByCategoryIdSortedUserRating() {
        // Arrange
        when(restaurantRepository.findAllByCategoryIdOrderByUserRating(anyLong())).thenReturn(testRestaurants);

        // Act
        List<Restaurant> result = restaurantService.getAllByCategoryIdSorted(1L, "userRating");

        // Assert
        assertThat(result).hasSize(2);
        verify(restaurantRepository, times(1)).findAllByCategoryIdOrderByUserRating(1L);
    }

    @Test
    @DisplayName("Get restaurants by category ID sorted by name")
    void getAllByCategoryIdSortedName() {
        // Arrange
        when(restaurantRepository.findAllByCategoryIdOrderByName(anyLong())).thenReturn(testRestaurants);

        // Act
        List<Restaurant> result = restaurantService.getAllByCategoryIdSorted(1L, "name");

        // Assert
        assertThat(result).hasSize(2);
        verify(restaurantRepository, times(1)).findAllByCategoryIdOrderByName(1L);
    }

    @Test
    @DisplayName("Get restaurant by ID")
    void getRestaurantsById() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));

        // Act
        Optional<Restaurant> result = restaurantService.getRestaurantsById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Restaurant", result.get().getName());
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Find all restaurants by category ID")
    void findAllByCategoryId() {
        // Arrange
        when(restaurantRepository.findAllByCategoryId(anyLong())).thenReturn(testRestaurants);

        // Act
        List<Restaurant> result = restaurantService.findAllByCategoryId(1L);

        // Assert
        assertThat(result).hasSize(2);
        verify(restaurantRepository, times(1)).findAllByCategoryId(1L);
    }

    @Test
    @DisplayName("Recommend random restaurant excluding categories - with results")
    void recommendRandomRestaurantExcludingCategories_WithResults() {
        // Arrange
        when(restaurantRepository.findAll(any(Sort.class))).thenReturn(testRestaurants);

        // Act
        Optional<Restaurant> result = restaurantService.recommendRandomRestaurantExcludingCategories("Chinese");

        // Assert
        assertTrue(result.isPresent());
        verify(restaurantRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Recommend random restaurant excluding categories - no results")
    void recommendRandomRestaurantExcludingCategories_NoResults() {
        // Arrange
        when(restaurantRepository.findAll(any(Sort.class))).thenReturn(testRestaurants);

        // Act
        Optional<Restaurant> result = restaurantService.recommendRandomRestaurantExcludingCategories("Korean");

        // Assert
        assertFalse(result.isPresent());
        verify(restaurantRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Delete restaurant by ID")
    void deleteById() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        doNothing().when(s3Service).deleteFileByURL(anyString());
        doNothing().when(restaurantRepository).deleteById(anyLong());

        // Act
        restaurantService.delete(1L);

        // Assert
        verify(restaurantRepository, times(1)).findById(1L);
        verify(s3Service, times(1)).deleteFileByURL("https://test-photo-url.jpg");
        verify(restaurantRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Find restaurant by ID")
    void findById() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));

        // Act
        Restaurant result = restaurantService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Find restaurant by name")
    void findByName() {
        // Arrange
        when(restaurantRepository.findByName(anyString())).thenReturn(Optional.of(testRestaurant));

        // Act
        Restaurant result = restaurantService.findByName("Test Restaurant");

        // Assert
        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        verify(restaurantRepository, times(1)).findByName("Test Restaurant");
    }

    @Test
    @DisplayName("Get all restaurants DTO")
    void getAllRestaurantsDTO() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> page = new PageImpl<>(testRestaurants, pageable, testRestaurants.size());
        when(restaurantRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        PageResponse<RestaurantDTO> result = restaurantService.getAllRestaurantsDTO(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(restaurantRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Create restaurant DTO")
    void createRestaurantDTO() {
        // Act
        RestaurantDTO result = restaurantService.createRestaurantDTO(testRestaurant);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Restaurant", result.getName());
        assertEquals("Test Address", result.getAddress());
        assertEquals("123-456-7890", result.getPhoneNumber());
        assertEquals(4.5, result.getRating());
        assertEquals(4.2, result.getUserRating());
        assertEquals("https://test-photo-url.jpg", result.getPhoto());
        assertEquals(1, result.getCategories().size());
        assertEquals("Korean", result.getCategories().get(0).getName());
    }
}