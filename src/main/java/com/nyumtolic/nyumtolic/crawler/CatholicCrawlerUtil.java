package com.nyumtolic.nyumtolic.crawler;

import com.nyumtolic.nyumtolic.controller.RestaurantController;
import groovy.util.logging.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
// @EnableScheduling
public class CatholicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    // @Scheduled(cron = "0 0 0 * * SAT") // 매주 토요일 00:00에 실행
    public void crawlCafeTable() {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
