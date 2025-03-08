package com.nyumtolic.nyumtolic.post;

import com.nyumtolic.nyumtolic.post.notice.NoticePost;
import com.nyumtolic.nyumtolic.post.notice.NoticePostService;
import com.nyumtolic.nyumtolic.post.user.UserPost;
import com.nyumtolic.nyumtolic.post.user.UserPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final BasePostService basePostService;
    private final NoticePostService noticePostService;
    private final UserPostService userPostService;

    // 공지사항 목록
    @GetMapping("/notices")
    public String noticeList(Model model) {
        List<BasePost> notices = basePostService.getPostsByType(NoticePost.class);
        List<NoticePost> pinned = noticePostService.getPinnedNotices();
        model.addAttribute("notices", notices);
        model.addAttribute("pinnedNotices", pinned);
        return "post/notice-list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notices/create")
    public String showNoticeForm(Model model) {
        model.addAttribute("noticePost", new NoticePost());
        return "post/notice-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notices/create")
    public String createNotice(@ModelAttribute NoticePost noticePost) {
        // 필요하다면 현재 로그인한 사용자를 noticePost에 설정
        basePostService.createPost(noticePost); // 혹은 noticePostService.createNotice(noticePost);
        return "redirect:/posts/notices";
    }


    // 공지사항 상세 조회 페이지
    @GetMapping("/notices/{id}")
    public String noticeDetail(@PathVariable Long id, Model model) {
        NoticePost notice = noticePostService.getNoticeById(id);
        model.addAttribute("notice", notice);
        return "post/notice-detail";
    }

    // 공지 수정 폼 보여주기
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notices/{id}/edit")
    public String editNotice(@PathVariable Long id, Model model) {
        NoticePost notice = noticePostService.getNoticeById(id);
        model.addAttribute("notice", notice);
        return "post/notice-edit";
    }

    // 공지 수정 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notices/{id}/edit")
    public String updateNotice(@PathVariable Long id, @ModelAttribute NoticePost updatedNotice) {
        noticePostService.updateNotice(id, updatedNotice);
        return "redirect:/posts/notices";
    }

    // 공지 삭제 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notices/{id}/delete")
    public String deleteNotice(@PathVariable Long id) {
        noticePostService.deleteNotice(id);
        return "redirect:/posts/notices";
    }


    /*// 유저 게시판 목록
    @GetMapping("/user-board")
    public String userBoardList(Model model) {
        List<UserPost> userPosts = userPostService.getUserPosts();
        model.addAttribute("userPosts", userPosts);
        return "post/user-board-list";
    }

    // 유저 게시글 작성 폼
    @GetMapping("/user-board/create")
    public String showUserPostForm(Model model) {
        model.addAttribute("userPost", new UserPost());
        return "post/user-board-form";
    }

    // 유저 게시글 생성 처리
    @PostMapping("/user-board/create")
    public String createUserPost(@ModelAttribute UserPost userPost) {
        // 로그인한 유저 정보를 가져와 작성자로 설정하는 로직 추가 가능 (ex: userPost.setAuthor(loggedInUser))
        basePostService.createPost(userPost); // 또는 userPostService.createUserPost(userPost);
        return "redirect:/posts/user-board";
    }*/

}
