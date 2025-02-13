package com.nyumtolic.nyumtolic.post.notice;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticePostService {

    private final NoticePostRepository noticePostRepository;

    // 상단 고정 공지사항 조회
    public List<NoticePost> getPinnedNotices() {
        return noticePostRepository.findByIsPinnedTrueOrderByCreateDateDesc();
    }

    // 공지사항 고정 설정
    @Transactional
    public void setPinned(Long noticeId, boolean isPinned) {
        NoticePost notice = noticePostRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항 없음"));
        notice.setPinned(isPinned);
    }

    @Transactional
    public NoticePost createNotice(NoticePost noticePost) {
        return noticePostRepository.save(noticePost);
    }
}
