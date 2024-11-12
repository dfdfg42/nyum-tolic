package com.nyumtolic.nyumtolic.controller;

import com.nyumtolic.nyumtolic.domain.VisitLog;
import com.nyumtolic.nyumtolic.service.VisitLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visit-logs")
@RequiredArgsConstructor
public class VisitLogController {

    private final VisitLogService visitLogService;

    @GetMapping
    public List<VisitLog> getAllVisitLogs() {
        return visitLogService.getAllVisitLogs();  // 모든 방문 기록
    }

    @GetMapping("/user/{userId}")
    public List<VisitLog> getVisitLogsByUser(@PathVariable Long userId) {
        return visitLogService.getVisitLogsByUserId(userId);  // 특정 사용자의 방문 기록
    }
}