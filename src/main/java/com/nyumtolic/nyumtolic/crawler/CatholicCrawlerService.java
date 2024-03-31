package com.nyumtolic.nyumtolic.crawler;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@EnableScheduling
@AllArgsConstructor
public class CatholicCrawlerService {

    private final CatholicCafeTableRepository catholicCafeTableRepository;

    @PostConstruct // 서버 실행시 최초 초기화
    public void onStartup() {
        updateData();
    }

    @Scheduled(cron = "0 0 0 * * SAT") // 매주 토요일 00:00에 실행
    public void onSchedule() {
        updateData();
    }

    public void updateData() {
        Map<String, String> data = CatholicCrawlerUtil.crawlCafeTable();
        CatholicCafeTable catholicCafeTable = getCatholicCafeInfo();

        if (catholicCafeTable == null) {
            catholicCafeTable = CatholicCafeTable.builder()
                    .buonPranzo(data.get("Buon Pranzo"))
                    .cafeBona(data.get("Cafe Bona"))
                    .cafeMensa(data.get("Cafe Mensa"))
                    .build();
        } else {
            catholicCafeTable.setBuonPranzo(data.get("Buon Pranzo"));
            catholicCafeTable.setCafeBona(data.get("Cafe Bona"));
            catholicCafeTable.setCafeMensa(data.get("Cafe Mensa"));
        }

        catholicCafeTableRepository.save(catholicCafeTable);

    }

    public CatholicCafeTable getCatholicCafeInfo() {
        return catholicCafeTableRepository.findById(1L).orElse(null);
    }
}
