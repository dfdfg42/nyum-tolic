package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;
    private SiteUser testUser;
    private Restaurant testRestaurant;
    private Review testReview;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() throws IOException {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();

        // Set up test user
        testUser = new SiteUser();
        testUser.setId(1L);
        testUser.setNickname("Test User");
        testUser.setLoginId("testuser@example.com");

        // Set up test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");

        // Set up test review
        testReview = new Review();
        testReview.setId(1);
        testReview.setContent("Great food and service!");
        testReview.setRating(4.5);
        testReview.setCreateDate(LocalDateTime.now());
        testReview.setRestaurant(testRestaurant);
        testReview.setAuthor(testUser);
        testReview.setImageUrl("https://test-image-url.jpg");
        testReview.setVoter(new HashSet<>());

        // Set up test image
        testImage = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        // Mock principal
        when(principal.getName()).thenReturn("testuser@example.com");
        
        // Setup mocks for methods that throw IOException
        doNothing().when(reviewService).create(anyLong(), anyString(), anyDouble(), any(SiteUser.class), any());
        doNothing().when(reviewService).modify(any(Review.class), anyString(), anyDouble(), any());
        doNothing().when(reviewService).deleteReviewImage(anyLong());
    }

    @Test
    @DisplayName("Create review - success")
    void createReviewSuccess() throws IOException {
        // Arrange
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setContent("Great place!");
        reviewForm.setRating(4.5);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUser(anyString())).thenReturn(testUser);
        doNothing().when(reviewService).create(anyLong(), anyString(), anyDouble(), any(SiteUser.class), any());

        // Act
        String viewName = reviewController.createReview(1L, reviewForm, bindingResult, principal, testImage, null);

        // Assert
        assertEquals("redirect:/restaurant/detail/1", viewName);
        verify(userService, times(1)).getUser("testuser@example.com");
        verify(reviewService, times(1)).create(eq(1L), eq("Great place!"), eq(4.5), eq(testUser), eq(testImage));
    }

    @Test
    @DisplayName("Create review - validation errors")
    void createReviewValidationErrors() throws IOException {
        // Arrange
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setContent("Great place!");
        reviewForm.setRating(4.5);

        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = reviewController.createReview(1L, reviewForm, bindingResult, principal, testImage, null);

        // Assert
        assertEquals("redirect:/restaurant/detail/1", viewName);
        verify(userService, never()).getUser(anyString());
        verify(reviewService, never()).create(anyLong(), anyString(), anyDouble(), any(SiteUser.class), any());
    }

    @Test
    @DisplayName("Show modify form - owner")
    void showModifyFormOwner() throws IOException {
        // Arrange
        when(reviewService.getReview(anyLong())).thenReturn(testReview);

        // Act & Assert
        assertDoesNotThrow(() -> {
            String viewName = reviewController.showModifyForm(1L, null, principal);
            assertEquals("review_form", viewName);
        });
        
        verify(reviewService, times(1)).getReview(1L);
    }

    @Test
    @DisplayName("Show modify form - not owner")
    void showModifyFormNotOwner() throws IOException {
        // Arrange
        SiteUser otherUser = new SiteUser();
        otherUser.setLoginId("other@example.com");
        
        Review reviewByOtherUser = new Review();
        reviewByOtherUser.setAuthor(otherUser);
        
        when(reviewService.getReview(anyLong())).thenReturn(reviewByOtherUser);

        // Act & Assert
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            reviewController.showModifyForm(1L, null, principal);
        });
        
        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
        assertEquals(HttpStatus.FORBIDDEN, responseStatusException.getStatusCode());
        
        verify(reviewService, times(1)).getReview(1L);
    }

    @Test
    @DisplayName("Modify review - success")
    void modifyReviewSuccess() throws IOException {
        // Arrange
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setContent("Updated content!");
        reviewForm.setRating(5.0);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewService.getReview(anyLong())).thenReturn(testReview);
        doNothing().when(reviewService).modify(any(Review.class), anyString(), anyDouble(), any());

        // Act
        String viewName = reviewController.modifyReview(1L, reviewForm, bindingResult, principal, null, testImage);

        // Assert
        assertEquals("redirect:/restaurant/detail/1", viewName);
        verify(reviewService, times(1)).getReview(1L);
        verify(reviewService, times(1)).modify(eq(testReview), eq("Updated content!"), eq(5.0), eq(testImage));
    }

    @Test
    @DisplayName("Modify review - validation errors")
    void modifyReviewValidationErrors() throws IOException {
        // Arrange
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setContent("Updated content!");
        reviewForm.setRating(5.0);

        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = reviewController.modifyReview(1L, reviewForm, bindingResult, principal, null, testImage);

        // Assert
        assertEquals("redirect:/review/modify/1", viewName);
        verify(reviewService, never()).getReview(anyLong());
        verify(reviewService, never()).modify(any(Review.class), anyString(), anyDouble(), any());
    }

    @Test
    @DisplayName("Modify review - not owner")
    void modifyReviewNotOwner() throws IOException {
        // Arrange
        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setContent("Updated content!");
        reviewForm.setRating(5.0);

        SiteUser otherUser = new SiteUser();
        otherUser.setLoginId("other@example.com");
        
        Review reviewByOtherUser = new Review();
        reviewByOtherUser.setAuthor(otherUser);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewService.getReview(anyLong())).thenReturn(reviewByOtherUser);

        // Act & Assert
        Exception exception = assertThrows(ResponseStatusException.class, () -> 
            reviewController.modifyReview(1L, reviewForm, bindingResult, principal, null, testImage)
        );
        
        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
        assertEquals(HttpStatus.FORBIDDEN, responseStatusException.getStatusCode());
        
        verify(reviewService, times(1)).getReview(1L);
        verify(reviewService, never()).modify(any(Review.class), anyString(), anyDouble(), any());
    }

    @Test
    @DisplayName("Delete review - owner")
    void deleteReviewOwner() throws IOException {
        // Arrange
        when(reviewService.getReview(anyLong())).thenReturn(testReview);
        doNothing().when(reviewService).delete(any(Review.class));

        // Act
        String viewName = reviewController.deleteReview(1L, principal);

        // Assert
        assertEquals("redirect:/restaurant/detail/1", viewName);
        verify(reviewService, times(1)).getReview(1L);
        verify(reviewService, times(1)).delete(testReview);
    }

    @Test
    @DisplayName("Delete review - not owner")
    void deleteReviewNotOwner() throws IOException {
        // Arrange
        SiteUser otherUser = new SiteUser();
        otherUser.setLoginId("other@example.com");
        
        Review reviewByOtherUser = new Review();
        reviewByOtherUser.setAuthor(otherUser);
        
        when(reviewService.getReview(anyLong())).thenReturn(reviewByOtherUser);

        // Act & Assert
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            reviewController.deleteReview(1L, principal);
        });
        
        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
        assertEquals(HttpStatus.FORBIDDEN, responseStatusException.getStatusCode());
        
        verify(reviewService, times(1)).getReview(1L);
        verify(reviewService, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("Vote for review")
    void reviewVote() throws IOException {
        // Arrange
        when(reviewService.getReview(anyLong())).thenReturn(testReview);
        when(userService.getUser(anyString())).thenReturn(testUser);
        doNothing().when(reviewService).vote(any(Review.class), any(SiteUser.class));

        // Act
        String viewName = reviewController.reviewVote(principal, 1L);

        // Assert
        assertEquals("redirect:/restaurant/detail/1", viewName);
        verify(reviewService, times(1)).getReview(1L);
        verify(userService, times(1)).getUser("testuser@example.com");
        verify(reviewService, times(1)).vote(testReview, testUser);
    }

    @Test
    @DisplayName("Delete review image - owner")
    void deleteReviewImageOwner() throws IOException {
        // Arrange
        when(reviewService.getReview(anyLong())).thenReturn(testReview);
        doNothing().when(reviewService).deleteReviewImage(anyLong());

        // Act
        String viewName = reviewController.deleteReviewImage(1L, principal, null);

        // Assert
        assertEquals("redirect:/restaurant/detail/1", viewName);
        verify(reviewService, times(1)).getReview(1L);
        verify(reviewService, times(1)).deleteReviewImage(1L);
    }

    @Test
    @DisplayName("Delete review image - not owner")
    void deleteReviewImageNotOwner() throws IOException {
        // Arrange
        SiteUser otherUser = new SiteUser();
        otherUser.setLoginId("other@example.com");
        
        Review reviewByOtherUser = new Review();
        reviewByOtherUser.setAuthor(otherUser);
        
        when(reviewService.getReview(anyLong())).thenReturn(reviewByOtherUser);

        // Act & Assert
        Exception exception = assertThrows(ResponseStatusException.class, () -> 
            reviewController.deleteReviewImage(1L, principal, null)
        );
        
        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
        assertEquals(HttpStatus.FORBIDDEN, responseStatusException.getStatusCode());
        
        verify(reviewService, times(1)).getReview(1L);
        verify(reviewService, never()).deleteReviewImage(anyLong());
    }
}