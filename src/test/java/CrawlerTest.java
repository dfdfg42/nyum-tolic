import com.nyumtolic.nyumtolic.catholic.CatholicCrawlerUtil;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

public class CrawlerTest {

    @Test
    public void testCrawler() throws Exception {
        CatholicCrawlerUtil catholicCrawler = new CatholicCrawlerUtil();

        Assert.notNull(catholicCrawler.crawlCafeTable(), "크롤링에 실패했습니다.");
    }
}
