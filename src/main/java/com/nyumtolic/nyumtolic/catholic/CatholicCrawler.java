package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.controller.RestaurantController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatholicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    //추출 링크와 1대1로 매칭
    @Getter
    private static final String[] keys = {"buonPranzo", "cafeBona", "cafeMensa"};

    private static final String baseUrl = "https://www.catholic.ac.kr";
    private static final String pdfUrl = "/ko/campuslife/restaurant.do";
    private static final String fullUrl = baseUrl + pdfUrl;

    public static ByteArrayResource downloadPdf(String url) {
        //RestTemplate 객체를 생성해 HTTP GET 요청을 보냄
        RestTemplate restTemplate = new RestTemplate();
        // 응답을 byte 배열로 받아옴
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        //2xx(성공)인 경우 응답을 byte 배열로 받아옴
        if(response.getStatusCode().is2xxSuccessful()) {
            byte[] pdfBytes = response.getBody();
            return new ByteArrayResource(pdfBytes);
        } else {
            throw new RuntimeException("Failed to download file from URL");
        }
    }


    public static byte[] convertPdfToJpg(byte[] pdfBytes, int pageNumber) throws IOException {
        // ByteArrayInputStream 를 사용해PDF 의 byte 배열을 입력 스트림으로 변환
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFRenderer renderer = new PDFRenderer(document);

            //300DPI 해상도로 렌더링하여 BufferedImage 객체 얻기
            BufferedImage image = renderer.renderImageWithDPI(pageNumber, 300);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //jpg 이미지의 byte 배열 반환
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        }
    }

    public static Map<String, String> crawlCafeTable() {

        Map<String, String> links = new HashMap<>();

        try {
            //URL 문서 불러오기
            Document document = Jsoup.connect(fullUrl).get();
            //<a> 태그 중 클래스가 link-txt 인 요소들 선택
            Elements tags = document.select("div.restaurant a.link-txt");


            //각 태그를 순회하면서 tag.attr("href") 를 통해 상대 경로를 추출
            links = new HashMap<>();
            for (int i = 0; i < tags.size(); i++) {
                Element tag = tags.get(i);
                String link = baseUrl + tag.attr("href");
                links.put(keys[i], link);
            }
            logger.info("학식정보 url 크롤링 완료");
            return links;
        } catch (IOException e) {
            logger.warn("학식정보 url 크롤링 실패");
            return new HashMap<>();
        }

    }

}