package com.nyumtolic.nyumtolic.catholic;

import com.nyumtolic.nyumtolic.S3.S3Service;
import com.nyumtolic.nyumtolic.s3.CustomMultipartFile;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CatholicCrawlerService {

    private final CatholicCafeTableRepository catholicCafeTableRepository;

    private final S3Service s3Service;

    @PostConstruct // 서버 실행시 최초 초기화
    public void onStartup() {
        updateData();
    }

    @Scheduled(cron = "0 0 0 * * SAT") // 매주 토요일 00:00에 실행
    public void onSchedule() {
        updateData();
    }

    public void updateData() {
        Map<String, String> data = CatholicCrawler.crawlCafeTable();

        for (String key : data.keySet()) {
            String url = data.get(key);

            try {
                ByteArrayResource pdf = CatholicCrawler.downloadPdf(url);
                String s3Url = s3Service.uploadFile(new CustomMultipartFile(pdf.getByteArray(), key, "application/pdf"));

                Optional<CatholicCafeTable> cafe = catholicCafeTableRepository.findByName(key);
                CatholicCafeTable updatedCafe = cafe.orElseGet(() -> CatholicCafeTable.builder()
                        .name(key)
                        .link(url)
                        .s3Ling(s3Url)
                        .build());

                catholicCafeTableRepository.save(updatedCafe);
            } catch (Exception e) {
                Optional<CatholicCafeTable> cafe = catholicCafeTableRepository.findByName(key);
                CatholicCafeTable updatedCafe = cafe.orElseGet(() -> CatholicCafeTable.builder()
                        .name(key)
                        .link(url)
                        .s3Ling("")
                        .build());

                catholicCafeTableRepository.save(updatedCafe);
            }
        }
    }
}
