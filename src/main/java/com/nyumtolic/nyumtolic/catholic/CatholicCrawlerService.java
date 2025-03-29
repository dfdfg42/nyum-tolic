package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.s3.S3Service;
import com.nyumtolic.nyumtolic.controller.RestaurantController;
import com.nyumtolic.nyumtolic.s3.CustomMultipartFile;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CatholicCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    private final CatholicCafeTableRepository catholicCafeTableRepository;

    private final S3Service s3Service;

    @PostConstruct // 서버 실행시 최초 초기화
    public void onStartup() {
        updateData();
    }

    // 매일 자정(00:00)과 정오(12:00)에 실행
    @Scheduled(cron = "0 0 0,12 * * *")
    public void onSchedule() {
        updateData();
    }

    public void updateData() {
        Map<String, String> data = CatholicCrawler.crawlCafeTable();

        for (String key : data.keySet()) {
            String url = data.get(key);

            try {
                ByteArrayResource pdf = CatholicCrawler.downloadPdf(url);
                // 여러 페이지 업로드 URL을 담을 리스트
                List<String> uploadedUrls = new ArrayList<>();

                // cafeMensa 인 경우 2~4페이지(0-base index: 1, 2, 3)를 처리
                if ("cafeMensa".equals(key)) {
                    for (int pageIndex = 1; pageIndex <= 3; pageIndex++) {
                        byte[] jpgBytes = CatholicCrawler.convertPdfToJpg(pdf.getByteArray(), pageIndex);
                        // 파일명에 페이지 번호를 추가하여 S3에 업로드
                        String fileName = String.format("%s-page%d.jpg", key, pageIndex + 1);
                        String s3Url = s3Service.uploadFile(new CustomMultipartFile(jpgBytes, fileName, "image/jpeg"));
                        uploadedUrls.add(s3Url);
                    }
                } else {
                    // 다른 카페는 첫 페이지만 처리
                    byte[] jpgBytes = CatholicCrawler.convertPdfToJpg(pdf.getByteArray(), 0);
                    uploadedUrls.add(s3Service.uploadFile(new CustomMultipartFile(jpgBytes, key, "image/jpeg")));
                }

                // 업로드된 URL들을 comma로 구분해서 DB에 저장
                String finalS3Link = String.join(",", uploadedUrls);
                Optional<CatholicCafeTable> cafe = catholicCafeTableRepository.findByName(key);
                CatholicCafeTable updatedCafe = cafe.orElseGet(() -> CatholicCafeTable.builder()
                        .name(key)
                        .link(url)
                        .build());
                updatedCafe.setS3Link(finalS3Link);
                catholicCafeTableRepository.save(updatedCafe);
                logger.info(key + " 학식정보 업데이트 완료 (" + uploadedUrls.size() + " page(s))");

            } catch (Exception e) {
                Optional<CatholicCafeTable> cafe = catholicCafeTableRepository.findByName(key);
                CatholicCafeTable updatedCafe = cafe.orElseGet(() -> CatholicCafeTable.builder()
                        .name(key)
                        .link(url)
                        .build());
                updatedCafe.setS3Link("");
                catholicCafeTableRepository.save(updatedCafe);
                logger.warn(key + " 학식정보 업데이트 실패 " + url + " 이 유효하지 않습니다.");
            }
        }
    }

}
