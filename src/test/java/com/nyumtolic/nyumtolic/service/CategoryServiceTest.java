package com.nyumtolic.nyumtolic.service;

import com.nyumtolic.nyumtolic.domain.Category;
import com.nyumtolic.nyumtolic.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mainCategory;
    private Category otherCategory;

    @BeforeEach
    void setUp() {
        // Set up main category
        mainCategory = new Category();
        mainCategory.setId(1L);
        mainCategory.setName("Korean");
        mainCategory.setMainCategory(true);

        // Set up other category
        otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Chinese");
        otherCategory.setMainCategory(false);
    }

    @Test
    @DisplayName("Save category")
    void save() {
        // Arrange
        when(categoryRepository.save(any(Category.class))).thenReturn(mainCategory);

        // Act
        categoryService.save(mainCategory);

        // Assert
        verify(categoryRepository, times(1)).save(mainCategory);
    }

    @Test
    @DisplayName("Find all categories")
    void findAllCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(mainCategory, otherCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryService.findAllCategories();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(mainCategory, otherCategory);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Find category by ID")
    void findCategoryById() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mainCategory));

        // Act
        Category result = categoryService.findCategoryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mainCategory.getId(), result.getId());
        assertEquals(mainCategory.getName(), result.getName());
        assertTrue(result.isMainCategory());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Find category by ID - not found")
    void findCategoryByIdNotFound() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Category result = categoryService.findCategoryById(999L);

        // Assert
        assertNull(result);
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Find main categories")
    void findMainCategories() {
        // Arrange
        List<Category> mainCategories = Arrays.asList(mainCategory);
        when(categoryRepository.findByIsMainCategoryTrue()).thenReturn(mainCategories);

        // Act
        List<Category> result = categoryService.findMainCategories();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).contains(mainCategory);
        assertThat(result).doesNotContain(otherCategory);
        verify(categoryRepository, times(1)).findByIsMainCategoryTrue();
    }

    @Test
    @DisplayName("Find other categories")
    void findOtherCategories() {
        // Arrange
        List<Category> otherCategories = Arrays.asList(otherCategory);
        when(categoryRepository.findByIsMainCategoryFalse()).thenReturn(otherCategories);

        // Act
        List<Category> result = categoryService.findOtherCategories();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).contains(otherCategory);
        assertThat(result).doesNotContain(mainCategory);
        verify(categoryRepository, times(1)).findByIsMainCategoryFalse();
    }

    @Test
    @DisplayName("Find category by name")
    void findByName() {
        // Arrange
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(mainCategory));

        // Act
        Category result = categoryService.findByName("Korean");

        // Assert
        assertNotNull(result);
        assertEquals("Korean", result.getName());
        verify(categoryRepository, times(1)).findByName("Korean");
    }

    @Test
    @DisplayName("Find category by name - not found")
    void findByNameNotFound() {
        // Arrange
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act
        Category result = categoryService.findByName("Nonexistent");

        // Assert
        assertNull(result);
        verify(categoryRepository, times(1)).findByName("Nonexistent");
    }

    @Test
    @DisplayName("Find all categories alias method")
    void findAll() {
        // Arrange
        List<Category> categories = Arrays.asList(mainCategory, otherCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(mainCategory, otherCategory);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Delete category")
    void delete() {
        // Arrange
        doNothing().when(categoryRepository).delete(any(Category.class));

        // Act
        categoryService.delete(mainCategory);

        // Assert
        verify(categoryRepository, times(1)).delete(mainCategory);
    }
}