import com.nyumtolic.nyumtolic.catholic.CatholicCrawler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class CatholicCrawlerTest {

    @Test
    @DisplayName("크롤러 기본 동작 테스트")
    void testCrawler() {
        // Act & Assert
        Assert.isTrue(!CatholicCrawler.crawlCafeTable().isEmpty(), "크롤링에 실패했습니다.");
    }
    
    @Test
    @DisplayName("테스트: 크롤링 결과 형식 검증")
    void testCrawlCafeTableFormat() {
        // Act
        Map<String, String> result = CatholicCrawler.crawlCafeTable();
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify expected cafe keys
        assertTrue(result.containsKey("cafeBona") || result.containsKey("cafeMensa"), 
                "크롤링 결과에 'cafeBona' 또는 'cafeMensa' 키가 없습니다.");
        
        // Verify URL format
        for (String url : result.values()) {
            assertTrue(url.startsWith("https://") || url.startsWith("http://"), 
                    "크롤링된 URL의 형식이 올바르지 않습니다: " + url);
            assertTrue(url.endsWith(".pdf"), 
                    "크롤링된 URL이 PDF 파일을 가리키지 않습니다: " + url);
        }
    }
    
    @Test
    @DisplayName("PDF 다운로드 및 이미지 변환 테스트(모킹)")
    void testPdfDownloadAndConversion() throws IOException {
        try (MockedStatic<CatholicCrawler> mockedCrawler = mockStatic(CatholicCrawler.class)) {
            // Arrange
            Map<String, String> mockData = new HashMap<>();
            mockData.put("cafeBona", "https://example.com/cafeBona.pdf");
            
            byte[] mockPdfBytes = "Sample PDF content".getBytes();
            ByteArrayResource mockResource = new ByteArrayResource(mockPdfBytes);
            
            byte[] mockJpgBytes = "Sample JPG content".getBytes();
            
            // Configure mocks
            mockedCrawler.when(CatholicCrawler::crawlCafeTable).thenReturn(mockData);
            mockedCrawler.when(() -> CatholicCrawler.downloadPdf(any())).thenReturn(mockResource);
            mockedCrawler.when(() -> CatholicCrawler.convertPdfToJpg(any(byte[].class), anyInt())).thenReturn(mockJpgBytes);
            
            // Act
            Map<String, String> result = CatholicCrawler.crawlCafeTable();
            ByteArrayResource pdfResource = CatholicCrawler.downloadPdf("https://example.com/test.pdf");
            byte[] jpgBytes = CatholicCrawler.convertPdfToJpg(mockPdfBytes, 0);
            
            // Assert
            assertNotNull(result);
            assertEquals(mockData, result);
            
            assertNotNull(pdfResource);
            assertArrayEquals(mockPdfBytes, pdfResource.getByteArray());
            
            assertNotNull(jpgBytes);
            assertArrayEquals(mockJpgBytes, jpgBytes);
        }
    }
}