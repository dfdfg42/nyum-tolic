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
        String url = "https://www.catholic.ac.kr/www/campuslife41_1.html";
        try {
            Document document = Jsoup.connect(url).get();
            Elements tags = document.select("a.ibtn1.block");

            Map<String, String> links = new HashMap<>();
            String[] keys = {"Buon Pranzo", "Cafe Bona", "Cafe Mensa"};
            for (int i = 0; i < tags.size(); i++) {
                Element tag = tags.get(i);
                String pdfUrl = "https://www.catholic.ac.kr/" + tag.attr("href");
                links.put(keys[i], pdfUrl);
            }
            logger.info("학식정보 크롤링 완료");

            // PDF 파일 저장 및 이미지 변환
            for (Map.Entry<String, String> entry : links.entrySet()) {
                String pdfFileName = entry.getKey() + ".pdf"; // 변경된 경로
                downloadPdfFromLink(entry.getValue(), pdfFileName);
                String imageOutputPath = entry.getKey() + ".jpg"; // 변경된 경로
                convertPdfToImage(pdfFileName, imageOutputPath);
            }

            return links;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void downloadPdfFromLink(String pdfUrl, String fileName) throws IOException {
        try (InputStream in = new BufferedInputStream(new URL(pdfUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            logger.info(fileName + " 다운로드 완료");
        }
    }

    // PDF 파일을 이미지로 변환하고 저장하는 메소드
    public static void convertPdfToImage(String pdfFilePath, String outputImagePath) {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
                // 파일 이름을 PDF 이름, 페이지 번호와 함께 구성
                String fileName = outputImagePath.substring(0, outputImagePath.lastIndexOf('.')) + "_page_" + i + ".jpg";
                ImageIO.write(image, "JPEG", new File(fileName));
            }
            logger.info("이미지 변환 완료: " + outputImagePath);
        } catch (IOException e) {
            logger.error("PDF 변환 중 오류 발생", e);
        }
    }
}
