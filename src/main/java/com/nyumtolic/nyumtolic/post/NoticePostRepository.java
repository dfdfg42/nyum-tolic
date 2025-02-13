package com.nyumtolic.nyumtolic.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticePostRepository extends JpaRepository<NoticePost, Long> {

    // 상단 고정된 공지사항 조회
    List<NoticePost> findByIsPinnedTrueOrderByCreateDateDesc();

    // 현재 시간 기준 활성화된 공지사항
    @Query("SELECT n FROM NoticePost n WHERE n.startDate <= :now AND n.endDate >= :now")
    List<NoticePost> findActiveNotices(LocalDateTime now);
}