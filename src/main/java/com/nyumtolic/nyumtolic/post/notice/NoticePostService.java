package com.nyumtolic.nyumtolic.post.notice;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticePostService {

    private final NoticePostRepository noticePostRepository;

    // 상단 고정 공지사항 조회
    public List<NoticePost> getPinnedNotices() {
        return noticePostRepository.findByIsPinnedTrueOrderByCreateDateDesc();
    }

    // ID로 공지사항 상세 조회 추가
    public NoticePost getNoticeById(Long id) {
        return noticePostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
    }

    // 공지사항 생성
    @Transactional
    public NoticePost createNotice(NoticePost noticePost) {
        return noticePostRepository.save(noticePost);
    }

    // 공지사항 수정 기능 추가
    @Transactional
    public NoticePost updateNotice(Long id, NoticePost updatedNotice) {
        NoticePost notice = getNoticeById(id);
        notice.setTitle(updatedNotice.getTitle());
        notice.setContent(updatedNotice.getContent());
        notice.setPinned(updatedNotice.isPinned());
        notice.setStartDate(updatedNotice.getStartDate());
        notice.setEndDate(updatedNotice.getEndDate());
        // modifyDate는 @PreUpdate로 자동 갱신
        return notice;
    }

    // 공지사항 삭제 기능 추가
    @Transactional
    public void deleteNotice(Long id) {
        NoticePost notice = getNoticeById(id);
        noticePostRepository.delete(notice);
    }

    // 공지사항 고정 설정
    @Transactional
    public void setPinned(Long noticeId, boolean isPinned) {
        NoticePost notice = getNoticeById(noticeId);
        notice.setPinned(isPinned);
    }


    //매일 자정 고정된 공지 일자 지났는지 확인
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void unpinExpiredNotices() {
        LocalDateTime now = LocalDateTime.now();
        List<NoticePost> pinnedNotices = noticePostRepository.findByIsPinnedTrueOrderByCreateDateDesc();

        for (NoticePost notice : pinnedNotices) {
            if (notice.getEndDate().isBefore(now)) {
                notice.setPinned(false);
            }
        }
    }
}
