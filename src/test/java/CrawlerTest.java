import com.nyumtolic.nyumtolic.crawler.CatholicCrawlerUtil;
import org.junit.jupiter.api.Test;

public class CrawlerTest {

    @Test
    public void testCrawler() throws Exception {
        CatholicCrawlerUtil catholicCrawler = new CatholicCrawlerUtil();
        catholicCrawler.crawlCafeTable();

    }
}
