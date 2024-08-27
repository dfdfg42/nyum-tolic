package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.controller.RestaurantController;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CatholicCrawlerUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    public static Map<String, String> crawlCafeTable() {
        String url = "https://www.catholic.ac.kr/ko/campuslife/restaurant.do";

        try {
            Document document = Jsoup.connect(url).get();
            Elements tags = document.select("div.restaurant a.link-txt");

            Map<String, String> links = new HashMap<>();
            String[] keys = {"Buon Pranzo", "Cafe Bona", "Cafe Mensa"};
            for (int i = 0; i < tags.size(); i++) {
                Element tag = tags.get(i);
                String pdfUrl = "https://www.catholic.ac.kr" + tag.attr("href");
                links.put(keys[i], pdfUrl);
            }
            logger.info("학식정보 크롤링 완료");

            return links;
        } catch (IOException e) {
            logger.info("학식정보 크롤링 실패");
            return null;
        }

    }
}
