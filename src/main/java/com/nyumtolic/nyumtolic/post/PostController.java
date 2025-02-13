package com.nyumtolic.nyumtolic.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final BasePostService basePostService;
    private final NoticePostService noticePostService;

    // 공지사항 목록
    @GetMapping("/notices")
    public String noticeList(Model model) {
        List<BasePost> notices = basePostService.getPostsByType(NoticePost.class);
        List<NoticePost> pinned = noticePostService.getPinnedNotices();
        model.addAttribute("notices", notices);
        model.addAttribute("pinnedNotices", pinned);
        return "post/notice-list";
    }

    // 일반 게시판 목록 (추후 구현)
    @GetMapping("/user-board")
    public String userBoardList() {
        return "post/user-board-list";
    }

    @GetMapping("/notices/create")
    public String showNoticeForm(Model model) {
        model.addAttribute("noticePost", new NoticePost());
        return "post/notice-form";
    }

    @PostMapping("/notices/create")
    public String createNotice(@ModelAttribute NoticePost noticePost) {
        // 필요하다면 현재 로그인한 사용자를 noticePost에 설정
        basePostService.createPost(noticePost); // 혹은 noticePostService.createNotice(noticePost);
        return "redirect:/posts/notices";
    }

}
