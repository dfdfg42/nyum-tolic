package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.s3.CustomMultipartFile;
import com.nyumtolic.nyumtolic.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatholicCrawlerServiceTest {

    @Mock
    private CatholicCafeTableRepository catholicCafeTableRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private CatholicCrawlerService catholicCrawlerService;

    private Map<String, String> crawlData;
    private ByteArrayResource pdfResource;

    @BeforeEach
    void setUp() {
        // Set up test data
        crawlData = new HashMap<>();
        crawlData.put("cafeBona", "https://example.com/cafeBona.pdf");
        crawlData.put("cafeMensa", "https://example.com/cafeMensa.pdf");

        // Mock PDF resource
        byte[] pdfBytes = "sample pdf content".getBytes();
        pdfResource = new ByteArrayResource(pdfBytes);
    }

    @Test
    @DisplayName("Update data for regular cafes")
    void updateDataForRegularCafes() throws Exception {
        // Arrange
        try (MockedStatic<CatholicCrawler> mockedCrawler = Mockito.mockStatic(CatholicCrawler.class)) {
            // Mock static methods
            mockedCrawler.when(CatholicCrawler::crawlCafeTable).thenReturn(crawlData);
            mockedCrawler.when(() -> CatholicCrawler.downloadPdf(anyString())).thenReturn(pdfResource);
            mockedCrawler.when(() -> CatholicCrawler.convertPdfToJpg(any(byte[].class), anyInt())).thenReturn("jpg content".getBytes());

            // Mock repository
            when(catholicCafeTableRepository.findByName("cafeBona")).thenReturn(Optional.empty());
            when(s3Service.uploadFile(any(CustomMultipartFile.class))).thenReturn("https://s3-url.com/cafeBona.jpg");

            // Act
            catholicCrawlerService.updateData();

            // Assert
            verify(catholicCafeTableRepository, times(1)).findByName("cafeBona");
            verify(s3Service, times(1)).uploadFile(any(CustomMultipartFile.class));
            verify(catholicCafeTableRepository, times(1)).save(argThat(cafe -> 
                "cafeBona".equals(cafe.getName()) && 
                "https://example.com/cafeBona.pdf".equals(cafe.getLink()) &&
                "https://s3-url.com/cafeBona.jpg".equals(cafe.getS3Link())
            ));
        }
    }

    @Test
    @DisplayName("Update data for cafeMensa with multiple pages")
    void updateDataForCafeMensaWithMultiplePages() throws Exception {
        // Arrange
        try (MockedStatic<CatholicCrawler> mockedCrawler = Mockito.mockStatic(CatholicCrawler.class)) {
            // Setup a map with only cafeMensa
            Map<String, String> cafeMensaData = new HashMap<>();
            cafeMensaData.put("cafeMensa", "https://example.com/cafeMensa.pdf");
            
            // Mock static methods
            mockedCrawler.when(CatholicCrawler::crawlCafeTable).thenReturn(cafeMensaData);
            mockedCrawler.when(() -> CatholicCrawler.downloadPdf(anyString())).thenReturn(pdfResource);
            mockedCrawler.when(() -> CatholicCrawler.convertPdfToJpg(any(byte[].class), anyInt())).thenReturn("jpg content".getBytes());

            // Mock repository
            when(catholicCafeTableRepository.findByName("cafeMensa")).thenReturn(Optional.empty());
            when(s3Service.uploadFile(any(CustomMultipartFile.class)))
                .thenReturn("https://s3-url.com/cafeMensa-page2.jpg")
                .thenReturn("https://s3-url.com/cafeMensa-page3.jpg")
                .thenReturn("https://s3-url.com/cafeMensa-page4.jpg");

            // Act
            catholicCrawlerService.updateData();

            // Assert
            verify(catholicCafeTableRepository, times(1)).findByName("cafeMensa");
            // cafeMensa should have 3 pages (pages 2-4)
            verify(s3Service, times(3)).uploadFile(any(CustomMultipartFile.class));
            verify(catholicCafeTableRepository, times(1)).save(argThat(cafe -> 
                "cafeMensa".equals(cafe.getName()) && 
                "https://example.com/cafeMensa.pdf".equals(cafe.getLink()) &&
                cafe.getS3Link().contains("https://s3-url.com/cafeMensa-page2.jpg") &&
                cafe.getS3Link().contains("https://s3-url.com/cafeMensa-page3.jpg") &&
                cafe.getS3Link().contains("https://s3-url.com/cafeMensa-page4.jpg")
            ));
        }
    }

    @Test
    @DisplayName("Update data when PDF download fails")
    void updateDataWhenPdfDownloadFails() throws Exception {
        // Arrange
        try (MockedStatic<CatholicCrawler> mockedCrawler = Mockito.mockStatic(CatholicCrawler.class)) {
            // Mock static methods
            mockedCrawler.when(CatholicCrawler::crawlCafeTable).thenReturn(crawlData);
            mockedCrawler.when(() -> CatholicCrawler.downloadPdf(anyString())).thenThrow(new RuntimeException("Download failed"));

            // Mock repository
            when(catholicCafeTableRepository.findByName(anyString())).thenReturn(Optional.empty());

            // Act
            catholicCrawlerService.updateData();

            // Assert
            verify(catholicCafeTableRepository, times(2)).findByName(anyString());
            verify(s3Service, never()).uploadFile(any(CustomMultipartFile.class));
            verify(catholicCafeTableRepository, times(2)).save(argThat(cafe -> 
                cafe.getS3Link().isEmpty()
            ));
        }
    }

    @Test
    @DisplayName("Update existing cafe data")
    void updateExistingCafeData() throws Exception {
        // Arrange
        try (MockedStatic<CatholicCrawler> mockedCrawler = Mockito.mockStatic(CatholicCrawler.class)) {
            // Mock static methods
            mockedCrawler.when(CatholicCrawler::crawlCafeTable).thenReturn(crawlData);
            mockedCrawler.when(() -> CatholicCrawler.downloadPdf(anyString())).thenReturn(pdfResource);
            mockedCrawler.when(() -> CatholicCrawler.convertPdfToJpg(any(byte[].class), anyInt())).thenReturn("jpg content".getBytes());

            // Create existing cafe
            CatholicCafeTable existingCafe = CatholicCafeTable.builder()
                    .name("cafeBona")
                    .link("https://example.com/old-cafeBona.pdf")
                    .s3Link("https://s3-url.com/old-cafeBona.jpg")
                    .build();

            // Mock repository
            when(catholicCafeTableRepository.findByName("cafeBona")).thenReturn(Optional.of(existingCafe));
            when(s3Service.uploadFile(any(CustomMultipartFile.class))).thenReturn("https://s3-url.com/new-cafeBona.jpg");

            // Act
            catholicCrawlerService.updateData();

            // Assert
            verify(catholicCafeTableRepository, times(1)).findByName("cafeBona");
            verify(s3Service, times(1)).uploadFile(any(CustomMultipartFile.class));
            verify(catholicCafeTableRepository, times(1)).save(argThat(cafe -> 
                "cafeBona".equals(cafe.getName()) && 
                "https://example.com/cafeBona.pdf".equals(cafe.getLink()) &&
                "https://s3-url.com/new-cafeBona.jpg".equals(cafe.getS3Link())
            ));
        }
    }
}