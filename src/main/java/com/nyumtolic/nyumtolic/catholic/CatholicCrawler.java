package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.controller.RestaurantController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatholicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Getter
    private static final String[] keys = {"Buon-Pranzo", "Cafe-Bona", "Cafe-Mensa"};

    private static final String baseUrl = "https://www.catholic.ac.kr";
    private static final String pdfUrl = "/ko/campuslife/restaurant.do";
    private static final String fullUrl = baseUrl + pdfUrl;

    public static ByteArrayResource downloadPdf(String url) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        if(response.getStatusCode().is2xxSuccessful()) {
            byte[] pdfBytes = response.getBody();
            return new ByteArrayResource(pdfBytes);
        } else {
            throw new RuntimeException("Failed to download file from URL");
        }
    }

    public static Map<String, String> crawlCafeTable() {

        Map<String, String> links = new HashMap<>();

        try {
            Document document = Jsoup.connect(fullUrl).get();
            Elements tags = document.select("div.restaurant a.link-txt");

            links = new HashMap<>();
            for (int i = 0; i < tags.size(); i++) {
                Element tag = tags.get(i);
                String link = baseUrl + tag.attr("href");
                links.put(keys[i], link);
            }
            logger.info("학식정보 크롤링 완료");
            return links;
        } catch (IOException e) {
            logger.info("학식정보 크롤링 실패");
            return new HashMap<>();
        }

    }

}