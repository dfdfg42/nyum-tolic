package com.nyumtolic.nyumtolic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataNotFoundExceptionTest {

    @Test
    @DisplayName("Test DataNotFoundException with message")
    void testDataNotFoundExceptionWithMessage() {
        // Arrange
        String errorMessage = "Data not found";
        
        // Act
        DataNotFoundException exception = new DataNotFoundException(errorMessage);
        
        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test DataNotFoundException without message")
    void testDataNotFoundExceptionWithoutMessage() {
        // Act
        DataNotFoundException exception = new DataNotFoundException(null);
        
        // Assert
        assertNull(exception.getMessage());
    }
}