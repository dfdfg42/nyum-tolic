package com.nyumtolic.nyumtolic.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    @InjectMocks
    private S3Service s3Service;

    private MockMultipartFile testFile;
    private String bucketName;
    private String region;

    @BeforeEach
    void setUp() {
        // Set up test file
        testFile = new MockMultipartFile(
                "testFile", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        // Set bucket name using reflection
        bucketName = "test-bucket";
        region = "ap-northeast-2";
        ReflectionTestUtils.setField(s3Service, "bucket", bucketName);
        
        // Mock region name
        when(amazonS3Client.getRegionName()).thenReturn(region);
    }

    @Test
    @DisplayName("Upload file with original filename")
    void uploadFile() throws IOException {
        // Arrange
        doNothing().when(amazonS3Client).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        
        // Act
        String result = s3Service.uploadFile(testFile);
        
        // Assert
        assertNotNull(result);
        assertEquals("https://" + bucketName + ".s3." + region + ".amazonaws.com/test.jpg", result);
        
        verify(amazonS3Client, times(1)).getRegionName();
        verify(amazonS3Client, times(1)).putObject(
                eq(bucketName), 
                eq("test.jpg"), 
                any(InputStream.class), 
                argThat(metadata -> 
                    metadata.getContentType().equals("image/jpeg") && 
                    metadata.getContentLength() == testFile.getSize()
                )
        );
    }

    @Test
    @DisplayName("Upload file with custom filename")
    void uploadFileWithName() throws IOException {
        // Arrange
        String customFileName = "custom-name.jpg";
        doNothing().when(amazonS3Client).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        
        // Act
        String result = s3Service.uploadFileWithName(testFile, customFileName);
        
        // Assert
        assertNotNull(result);
        assertEquals("https://" + bucketName + ".s3." + region + ".amazonaws.com/" + customFileName, result);
        
        verify(amazonS3Client, times(1)).getRegionName();
        verify(amazonS3Client, times(1)).putObject(
                eq(bucketName), 
                eq(customFileName), 
                any(InputStream.class), 
                argThat(metadata -> 
                    metadata.getContentType().equals("image/jpeg") && 
                    metadata.getContentLength() == testFile.getSize()
                )
        );
    }

    @Test
    @DisplayName("Delete file by filename")
    void deleteFile() {
        // Arrange
        String fileName = "test.jpg";
        doNothing().when(amazonS3Client).deleteObject(anyString(), anyString());
        
        // Act
        s3Service.deleteFile(fileName);
        
        // Assert
        verify(amazonS3Client, times(1)).deleteObject(bucketName, fileName);
    }

    @Test
    @DisplayName("Delete file by URL")
    void deleteFileByURL() {
        // Arrange
        String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/test.jpg";
        doNothing().when(amazonS3Client).deleteObject(anyString(), anyString());
        
        // Act
        s3Service.deleteFileByURL(fileUrl);
        
        // Assert
        verify(amazonS3Client, times(1)).deleteObject(bucketName, "test.jpg");
    }

    @Test
    @DisplayName("Handle IOException during upload")
    void handleIOExceptionDuringUpload() throws IOException {
        // Arrange
        when(amazonS3Client.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                .thenThrow(new RuntimeException("S3 error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> s3Service.uploadFile(testFile));
        
        verify(amazonS3Client, times(1)).getRegionName();
        verify(amazonS3Client, times(1)).putObject(
                eq(bucketName), 
                anyString(), 
                any(InputStream.class), 
                any(ObjectMetadata.class)
        );
    }
}