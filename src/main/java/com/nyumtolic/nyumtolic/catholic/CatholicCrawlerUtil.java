package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.controller.RestaurantController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CatholicCrawlerUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    public static Map<String, String> crawlCafeTable() {
        String url = "https://www.catholic.ac.kr/www/campuslife41_1.html";
        try {
            Document document = Jsoup.connect(url).get();
            Elements tags = document.select("a.ibtn1.block");

            Map<String, String> links = new HashMap<>();
            String[] keys = {"Buon Pranzo", "Cafe Bona", "Cafe Mensa"};
            for (int i = 0; i < tags.size(); i++) {
                Element tag = tags.get(i);
                String link = "https://www.catholic.ac.kr/" + tag.attr("href");
                links.put(keys[i], link);
            }
            logger.info("학식정보 크롤링 완료");

            return links;

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }
}
