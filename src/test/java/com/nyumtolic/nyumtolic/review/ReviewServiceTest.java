package com.nyumtolic.nyumtolic.review;

import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.domain.ReviewLog;
import com.nyumtolic.nyumtolic.repository.RestaurantRepository;
import com.nyumtolic.nyumtolic.repository.ReviewLogRepository;
import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ReviewLogRepository reviewLogRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Restaurant testRestaurant;
    private SiteUser testUser;
    private Review testReview;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        // Set up test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setUserRating(4.0);

        // Set up test user
        testUser = new SiteUser();
        testUser.setId(1L);
        testUser.setNickname("Test User");
        testUser.setLoginId("testuser@example.com");

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
    }

    @Test
    @DisplayName("Vote for a review")
    void vote() {
        // Act
        reviewService.vote(testReview, testUser);

        // Assert
        assertThat(testReview.getVoter()).contains(testUser);
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    @DisplayName("Create a new review")
    void create() throws IOException {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));
        when(s3Service.uploadFileWithName(any(MultipartFile.class), anyString())).thenReturn("https://new-image-url.jpg");

        // Act
        Review result = reviewService.create(1L, "Great place!", 4.5, testUser, testImage);

        // Assert
        assertNotNull(result);
        assertEquals("Great place!", result.getContent());
        assertEquals(4.5, result.getRating());
        assertEquals(testUser, result.getAuthor());
        assertEquals(testRestaurant, result.getRestaurant());
        assertEquals("https://new-image-url.jpg", result.getImageUrl());

        // Verify repository calls
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewLogRepository, times(1)).save(any(ReviewLog.class));

        // Verify restaurant rating update
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Create a review for nonexistent restaurant should throw exception")
    void createWithNonexistentRestaurant() {
        // Arrange
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class, () -> {
            reviewService.create(1L, "Great place!", 4.5, testUser, testImage);
        });

        // Verify no saves occurred
        verify(reviewRepository, never()).save(any(Review.class));
        verify(reviewLogRepository, never()).save(any(ReviewLog.class));
    }

    @Test
    @DisplayName("Get review by ID")
    void getReview() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(testReview));

        // Act
        Review result = reviewService.getReview(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testReview.getId(), result.getId());
        assertEquals(testReview.getContent(), result.getContent());
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get review by ID - not found")
    void getReviewNotFound() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class, () -> {
            reviewService.getReview(1L);
        });
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get user's reviews")
    void getUserReviews() {
        // Arrange
        List<Review> userReviews = Arrays.asList(testReview);
        when(reviewRepository.findByAuthorId(anyLong())).thenReturn(userReviews);

        // Act
        List<Review> result = reviewService.getUserReviews(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).contains(testReview);
        verify(reviewRepository, times(1)).findByAuthorId(1L);
    }

    @Test
    @DisplayName("Modify a review")
    void modify() throws IOException {
        // Arrange
        when(s3Service.uploadFileWithName(any(MultipartFile.class), anyString())).thenReturn("https://updated-image-url.jpg");

        // Act
        reviewService.modify(testReview, "Updated content", 5.0, testImage);

        // Assert
        assertEquals("Updated content", testReview.getContent());
        assertEquals(5.0, testReview.getRating());
        assertNotNull(testReview.getModifyDate());
        assertEquals("https://updated-image-url.jpg", testReview.getImageUrl());

        verify(s3Service, times(1)).deleteFileByURL(anyString());
        verify(s3Service, times(1)).uploadFileWithName(any(MultipartFile.class), anyString());
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    @DisplayName("Modify a review without changing the image")
    void modifyWithoutImage() throws IOException {
        // Arrange
        MockMultipartFile emptyImage = new MockMultipartFile(
                "image", "", "image/jpeg", new byte[0]);

        // Act
        reviewService.modify(testReview, "Updated content", 5.0, emptyImage);

        // Assert
        assertEquals("Updated content", testReview.getContent());
        assertEquals(5.0, testReview.getRating());
        assertNotNull(testReview.getModifyDate());
        assertEquals("https://test-image-url.jpg", testReview.getImageUrl()); // Image URL should remain unchanged

        verify(s3Service, never()).deleteFileByURL(anyString());
        verify(s3Service, never()).uploadFileWithName(any(MultipartFile.class), anyString());
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    @DisplayName("Delete a review")
    void delete() throws IOException {
        // Act
        reviewService.delete(testReview);

        // Assert
        verify(s3Service, times(1)).deleteFileByURL(anyString());
        verify(reviewRepository, times(1)).delete(testReview);
    }

    @Test
    @DisplayName("Find reviews by restaurant ID")
    void findReviewsByRestaurantId() {
        // Arrange
        List<Review> restaurantReviews = Arrays.asList(testReview);
        when(reviewRepository.findByRestaurantId(anyLong())).thenReturn(restaurantReviews);

        // Act
        List<Review> result = reviewService.findReviewsByRestaurantId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).contains(testReview);
        verify(reviewRepository, times(1)).findByRestaurantId(1L);
    }

    @Test
    @DisplayName("Find reviews with votes by restaurant ID")
    void findReviewsWithVotesByRestaurantId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Object[] reviewWithVotes = new Object[]{testReview, 5L}; // Review and vote count
        List<Object[]> content = new ArrayList<>();
        content.add(reviewWithVotes);
        Page<Object[]> page = new PageImpl<>(content, pageable, 1);

        when(reviewRepository.findReviewsAndVoteCountByRestaurantId(anyLong(), any(Pageable.class))).thenReturn(page);

        // Act
        Page<ReviewWithVotesDTO> result = reviewService.findReviewsWithVotesByRestaurantId(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertEquals(testReview, result.getContent().get(0).getReview());
        assertEquals(5L, result.getContent().get(0).getVotesCount());
        verify(reviewRepository, times(1)).findReviewsAndVoteCountByRestaurantId(1L, pageable);
    }

    @Test
    @DisplayName("Update restaurant user rating")
    void updateRestaurantUserRating() {
        // Arrange
        List<Review> reviews = Arrays.asList(
                createReviewWithRating(4.5),
                createReviewWithRating(3.5),
                createReviewWithRating(5.0)
        );
        when(reviewRepository.findByRestaurantId(anyLong())).thenReturn(reviews);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(testRestaurant));

        // Act
        reviewService.updateRestaurantUserRating(1L);

        // Assert
        ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository, times(1)).save(restaurantCaptor.capture());
        
        Restaurant savedRestaurant = restaurantCaptor.getValue();
        assertEquals(4.3, savedRestaurant.getUserRating(), 0.1); // Average of 4.5, 3.5, and 5.0 rounded to 1 decimal place
    }

    @Test
    @DisplayName("Delete review image")
    void deleteReviewImage() throws IOException {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(testReview));

        // Act
        reviewService.deleteReviewImage(1L);

        // Assert
        assertNull(testReview.getImageUrl());
        verify(s3Service, times(1)).deleteFileByURL(anyString());
        verify(reviewRepository, times(1)).save(testReview);
    }

    // Helper method
    private Review createReviewWithRating(double rating) {
        Review review = new Review();
        review.setRating(rating);
        return review;
    }
}