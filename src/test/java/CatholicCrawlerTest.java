import com.nyumtolic.nyumtolic.catholic.CatholicCrawler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

public class CatholicCrawlerTest {

    @Test
    @DisplayName("url 크롤러")
    void testCrawler() {
        Assert.isTrue(!CatholicCrawler.crawlCafeTable().isEmpty(), "크롤링에 실패했습니다.");
    }
}
