package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.cloudinary.CloudinaryImageService;
import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.post.notice.NoticePost;
import com.nyumtolic.nyumtolic.post.notice.NoticePostService;
import com.nyumtolic.nyumtolic.review.ReviewService;
import com.nyumtolic.nyumtolic.review.ReviewWithVotesDTO;
import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import com.nyumtolic.nyumtolic.service.CategoryService;
import com.nyumtolic.nyumtolic.service.RecommendationService;
import com.nyumtolic.nyumtolic.service.RestaurantService;
import com.nyumtolic.nyumtolic.service.VisitLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private VisitLogService visitLogService;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private S3Service s3Service;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private NoticePostService noticePostService;

    @Mock
    private CloudinaryImageService cloudinaryImageService;

    @Mock
    private Model model;

    @InjectMocks
    private RestaurantController restaurantController;

    private MockMvc mockMvc;
    private Restaurant testRestaurant;
    private List<Restaurant> testRestaurants;
    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(restaurantController).build();

        // Set up test categories
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Korean");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Chinese");

        testCategories = Arrays.asList(category1, category2);

        // Set up test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("Test Address");
        testRestaurant.setPhoneNumber("123-456-7890");
        testRestaurant.setRating(4.5);
        testRestaurant.setUserRating(4.2);
        testRestaurant.setPhoto("https://test-photo-url.jpg");
        testRestaurant.setCategories(Arrays.asList(category1));
        testRestaurant.setMenu(Arrays.asList("Menu Item 1", "Menu Item 2"));
        testRestaurant.setDescription("Test Description");
        testRestaurant.setTravelTime(10);
        testRestaurant.setLatitude(37.123);
        testRestaurant.setLongitude(127.456);
        testRestaurant.setReviews(new ArrayList<>());

        // Set up test restaurant list
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Another Restaurant");
        restaurant2.setCategories(Arrays.asList(category2));
        restaurant2.setUserRating(4.0);
        restaurant2.setReviews(new ArrayList<>());

        testRestaurants = Arrays.asList(testRestaurant, restaurant2);
    }

    @Test
    @DisplayName("Test detail view")
    void testDetailView() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        when(restaurantService.getRestaurantsById(restaurantId)).thenReturn(Optional.of(testRestaurant));
        
        Page<ReviewWithVotesDTO> reviewPage = new PageImpl<>(new ArrayList<>());
        when(reviewService.findReviewsWithVotesByRestaurantId(eq(restaurantId), any(Pageable.class)))
                .thenReturn(reviewPage);

        // Act & Assert
        mockMvc.perform(get("/restaurant/detail/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/detail"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("menuString"))
                .andExpect(model().attributeExists("categoryString"))
                .andExpect(model().attributeExists("reviewsPage"));

        verify(restaurantService, times(2)).getRestaurantsById(restaurantId);
        verify(reviewService, times(1)).findReviewsWithVotesByRestaurantId(eq(restaurantId), any(Pageable.class));
    }

    @Test
    @DisplayName("Test restaurant list view - no category")
    void testRestaurantListViewNoCategory() throws Exception {
        // Arrange
        when(restaurantService.getAllRestaurantsBySorted("name")).thenReturn(testRestaurants);
        when(noticePostService.getPinnedNotices()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/restaurant/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/list"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(model().attributeExists("pinnedNotices"));

        verify(restaurantService, times(1)).getAllRestaurantsBySorted("name");
        verify(noticePostService, times(1)).getPinnedNotices();
    }

    @Test
    @DisplayName("Test restaurant list view - with category")
    void testRestaurantListViewWithCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        when(restaurantService.getAllByCategoryIdSorted(categoryId, "name")).thenReturn(testRestaurants);
        when(noticePostService.getPinnedNotices()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/restaurant/list")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/list"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(model().attributeExists("categoryId"))
                .andExpect(model().attributeExists("pinnedNotices"));

        verify(restaurantService, times(1)).getAllByCategoryIdSorted(categoryId, "name");
        verify(noticePostService, times(1)).getPinnedNotices();
    }

    @Test
    @DisplayName("Test recommendation view - no excluded categories")
    void testRecommendationViewNoExcludedCategories() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/restaurant/recommendation"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/recommendation"))
                .andExpect(model().attribute("excludedCategories", ""));

        verify(restaurantService, never()).recommendRandomRestaurantExcludingCategories(any());
    }

    @Test
    @DisplayName("Test recommendation view - with excluded categories")
    void testRecommendationViewWithExcludedCategories() throws Exception {
        // Arrange
        String excludedCategories = "Korean, Chinese";
        when(restaurantService.recommendRandomRestaurantExcludingCategories(any())).thenReturn(Optional.of(testRestaurant));

        // Act & Assert
        mockMvc.perform(get("/restaurant/recommendation")
                        .param("excludedCategories", excludedCategories))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/recommendation"))
                .andExpect(model().attribute("excludedCategories", excludedCategories))
                .andExpect(model().attributeExists("recommendedRestaurant"));

        verify(restaurantService, times(1)).recommendRandomRestaurantExcludingCategories(any());
    }

    @Test
    @DisplayName("Test admin list view")
    void testAdminListView() throws Exception {
        // Arrange
        when(restaurantService.findAll()).thenReturn(testRestaurants);

        // Act & Assert
        mockMvc.perform(get("/restaurant/admin/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/admin/restaurant_admin_list"))
                .andExpect(model().attributeExists("restaurants"));

        verify(restaurantService, times(1)).findAll();
    }

    @Test
    @DisplayName("Test admin create form")
    void testAdminCreateForm() throws Exception {
        // Arrange
        when(categoryService.findAll()).thenReturn(testCategories);

        // Act & Assert
        mockMvc.perform(get("/restaurant/admin/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/admin/restaurant_admin_form"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("allCategories"));

        verify(categoryService, times(1)).findAll();
    }

    @Test
    @DisplayName("Test admin save restaurant - new restaurant")
    void testAdminSaveRestaurantNew() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "photoFile", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(s3Service.uploadFileWithName(any(), any())).thenReturn("https://new-photo-url.jpg");

        // Act
        String viewName = restaurantController.saveRestaurantForAdmin(testRestaurant, file);

        // Assert
        assertEquals("redirect:/restaurant/admin/list", viewName);
        verify(restaurantService, times(1)).save(any(Restaurant.class));
        verify(s3Service, times(1)).uploadFileWithName(any(), any());
    }

    @Test
    @DisplayName("Test admin edit form")
    void testAdminEditForm() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        when(restaurantService.findById(restaurantId)).thenReturn(testRestaurant);
        when(categoryService.findAll()).thenReturn(testCategories);

        // Act & Assert
        mockMvc.perform(get("/restaurant/admin/edit/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant/admin/restaurant_admin_form"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("allCategories"));

        verify(restaurantService, times(1)).findById(restaurantId);
        verify(categoryService, times(1)).findAll();
    }

    @Test
    @DisplayName("Test admin delete restaurant")
    void testAdminDeleteRestaurant() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        doNothing().when(restaurantService).delete(restaurantId);

        // Act & Assert
        mockMvc.perform(get("/restaurant/admin/delete/{id}", restaurantId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant/admin/list"));

        verify(restaurantService, times(1)).delete(restaurantId);
    }
}