package com.nyumtolic.nyumtolic.post;

import com.nyumtolic.nyumtolic.post.comment.Comment;
import com.nyumtolic.nyumtolic.post.comment.CommentService;
import com.nyumtolic.nyumtolic.post.notice.NoticePost;
import com.nyumtolic.nyumtolic.post.notice.NoticePostService;
import com.nyumtolic.nyumtolic.post.user.BoardCategory;
import com.nyumtolic.nyumtolic.post.user.UserPost;
import com.nyumtolic.nyumtolic.post.user.UserPostService;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final BasePostService basePostService;
    private final NoticePostService noticePostService;
    private final UserPostService userPostService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    // ========== 게시판 메인 페이지 ==========

    // 게시판 메인 페이지 (공지사항 + 유저게시판 카테고리 목록)
    @GetMapping("")
    public String boardMain(Model model) {
        // 최근 공지사항 (상위 3개)
        List<BasePost> recentNotices = basePostService.getPostsByTypeOrderByCreatedDateDesc(NoticePost.class)
                .stream().limit(3).collect(Collectors.toList());

        // 고정 공지사항
        List<NoticePost> pinnedNotices = noticePostService.getPinnedNotices();

        // 각 카테고리별 최근 게시글 (상위 3개씩)
        Map<BoardCategory, List<UserPost>> categoryPosts = new HashMap<>();
        for (BoardCategory category : BoardCategory.values()) {
            List<UserPost> posts = userPostService.getPostsByCategory(category.getUrlPath())
                    .stream().limit(3).collect(Collectors.toList());
            categoryPosts.put(category, posts);
        }

        model.addAttribute("recentNotices", recentNotices);
        model.addAttribute("pinnedNotices", pinnedNotices);
        model.addAttribute("boardCategories", BoardCategory.values());
        model.addAttribute("categoryPosts", categoryPosts);

        return "post/board-main";
    }

    // ========== 공지사항 관련 메서드들 ==========

    // 공지사항 목록
    @GetMapping("/notices")
    public String noticeList(Model model) {
        List<BasePost> notices = basePostService.getPostsByTypeOrderByCreatedDateDesc(NoticePost.class);
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
    public String createNotice(@ModelAttribute NoticePost noticePost, Principal principal) {
        SiteUser author = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        noticePost.setAuthor(author);
        basePostService.createPost(noticePost);

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

    // ========== 유저 게시판 관련 메서드들 ==========

    // 특정 카테고리 유저 게시판 목록 (페이징 추가)
    @GetMapping("/user-board/{category}")
    public String userBoardByCategory(@PathVariable String category,
                                      @PageableDefault(size = 10, sort = "createDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                                      Model model) {
        BoardCategory boardCategory = BoardCategory.fromUrlPath(category);
        Page<UserPost> userPostsPage = userPostService.getPostsByCategoryPaged(category, pageable);

        model.addAttribute("userPostsPage", userPostsPage);
        model.addAttribute("userPosts", userPostsPage.getContent());
        model.addAttribute("currentCategory", boardCategory);
        model.addAttribute("boardCategories", BoardCategory.values());

        return "post/user-board-list";
    }

    // 전체 유저 게시판 목록 (페이징 추가)
    @GetMapping("/user-board")
    public String userBoardList(@PageableDefault(size = 10, sort = "createDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                                Model model) {
        Page<UserPost> userPostsPage = userPostService.getUserPostsPaged(pageable);

        model.addAttribute("userPostsPage", userPostsPage);
        model.addAttribute("userPosts", userPostsPage.getContent());
        model.addAttribute("currentCategory", null); // 전체 카테고리
        model.addAttribute("boardCategories", BoardCategory.values());
        return "post/user-board-list";
    }

    // 유저 게시글 작성 폼
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user-board/create")
    public String showUserPostForm(@RequestParam(value = "category", required = false) String category, Model model) {
        UserPost userPost = new UserPost();
        if (category != null) {
            userPost.setCategory(category);
        }

        model.addAttribute("userPost", userPost);
        model.addAttribute("boardCategories", BoardCategory.values());
        model.addAttribute("selectedCategory", category);

        return "post/user-board-form";
    }

    // 유저 게시글 생성 처리
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user-board/create")
    public String createUserPost(@ModelAttribute UserPost userPost, Principal principal) {
        SiteUser author = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        userPost.setAuthor(author);

        // 카테고리가 지정되지 않았으면 자유게시판으로 설정
        if (userPost.getCategory() == null || userPost.getCategory().isEmpty()) {
            userPost.setCategory(BoardCategory.FREE.getUrlPath());
        }

        basePostService.createPost(userPost);

        // 해당 카테고리 게시판으로 리다이렉트
        return "redirect:/posts/user-board/" + userPost.getCategory();
    }

    // 유저 게시글 상세 조회
    @GetMapping("/user-board/{category}/{id}")
    public String userPostDetail(@PathVariable String category, @PathVariable Long id,
                                 @PageableDefault(size = 10, sort = "createDate") Pageable pageable,
                                 Model model,
                                 Principal principal) {
        UserPost userPost = userPostService.getUserPostById(id);
        BoardCategory boardCategory = BoardCategory.fromUrlPath(category);

        // 조회수 증가
        userPostService.incrementViewCount(id);

        // 댓글 목록 조회
        List<Comment> comments = commentService.getCommentsByPostId(id);

        // 각 댓글의 대댓글도 조회
        Map<Long, List<Comment>> repliesMap = new HashMap<>();
        for (Comment comment : comments) {
            List<Comment> replies = commentService.getRepliesByParentId(comment.getId());
            repliesMap.put(comment.getId(), replies);
        }

        // 현재 사용자 정보 (좋아요 상태 확인용)
        SiteUser currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByLoginId(principal.getName()).orElse(null);
        }

        model.addAttribute("userPost", userPost);
        model.addAttribute("currentCategory", boardCategory);
        model.addAttribute("comments", comments);
        model.addAttribute("repliesMap", repliesMap);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("commentCount", comments.size());

        return "post/user-board-detail";
    }

    // 유저 게시글 수정 폼
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user-board/{category}/{id}/edit")
    public String editUserPost(@PathVariable String category, @PathVariable Long id, Model model, Principal principal) {
        UserPost userPost = userPostService.getUserPostById(id);
        BoardCategory boardCategory = BoardCategory.fromUrlPath(category);

        // 작성자 본인만 수정 가능
        if (!userPost.getAuthor().getLoginId().equals(principal.getName())) {
            return "redirect:/posts/user-board/" + category;
        }

        model.addAttribute("userPost", userPost);
        model.addAttribute("currentCategory", boardCategory);
        model.addAttribute("boardCategories", BoardCategory.values());

        return "post/user-board-edit";
    }

    // 유저 게시글 수정 처리
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user-board/{category}/{id}/edit")
    public String updateUserPost(@PathVariable String category, @PathVariable Long id, @ModelAttribute UserPost updatedPost, Principal principal) {
        UserPost userPost = userPostService.getUserPostById(id);

        // 작성자 본인만 수정 가능
        if (!userPost.getAuthor().getLoginId().equals(principal.getName())) {
            return "redirect:/posts/user-board/" + category;
        }

        userPostService.updateUserPost(id, updatedPost);
        return "redirect:/posts/user-board/" + category + "/" + id;
    }

    // 유저 게시글 삭제
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user-board/{category}/{id}/delete")
    public String deleteUserPost(@PathVariable String category, @PathVariable Long id, Principal principal) {
        UserPost userPost = userPostService.getUserPostById(id);

        // 작성자 본인 또는 관리자만 삭제 가능
        if (!userPost.getAuthor().getLoginId().equals(principal.getName()) &&
                !principal.getName().equals("admin")) { // 또는 역할 기반 체크
            return "redirect:/posts/user-board/" + category;
        }

        userPostService.deleteUserPost(id);
        return "redirect:/posts/user-board/" + category;
    }

    // 게시글 좋아요 처리
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user-board/{category}/{id}/like")
    public String likeUserPost(@PathVariable String category, @PathVariable Long id, Principal principal) {
        SiteUser user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        userPostService.toggleLike(id, user);
        return "redirect:/posts/user-board/" + category + "/" + id;
    }
}