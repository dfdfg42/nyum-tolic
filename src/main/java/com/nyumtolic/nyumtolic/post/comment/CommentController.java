package com.nyumtolic.nyumtolic.post.comment;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import com.nyumtolic.nyumtolic.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;
    private final UserService userService;

    // 댓글 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{postId}")
    public String createComment(@PathVariable Long postId,
                                @RequestParam String content,
                                @RequestParam(defaultValue = "false") boolean isAnonymous,
                                @RequestParam String category,
                                Principal principal) {
        SiteUser author = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        commentService.createComment(postId, content, author, isAnonymous);

        return "redirect:/posts/user-board/" + category + "/" + postId;
    }

    // 대댓글 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reply/{postId}/{parentId}")
    public String createReply(@PathVariable Long postId,
                              @PathVariable Long parentId,
                              @RequestParam String content,
                              @RequestParam(defaultValue = "false") boolean isAnonymous,
                              @RequestParam String category,
                              Principal principal) {
        SiteUser author = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        commentService.createReply(postId, parentId, content, author, isAnonymous);

        return "redirect:/posts/user-board/" + category + "/" + postId;
    }

    // 댓글 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{commentId}")
    public String updateComment(@PathVariable Long commentId,
                                @RequestParam String content,
                                @RequestParam String category,
                                @RequestParam Long postId,
                                Principal principal) {
        Comment comment = commentService.getComment(commentId);

        // 작성자 본인만 수정 가능
        if (!comment.getAuthor().getLoginId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 수정 권한이 없습니다.");
        }

        commentService.updateComment(commentId, content);

        return "redirect:/posts/user-board/" + category + "/" + postId;
    }

    // 댓글 삭제
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam String category,
                                @RequestParam Long postId,
                                Principal principal) {
        Comment comment = commentService.getComment(commentId);

        // 작성자 본인 또는 관리자만 삭제 가능
        if (!comment.getAuthor().getLoginId().equals(principal.getName()) &&
                !hasAdminRole(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        commentService.deleteComment(commentId);

        return "redirect:/posts/user-board/" + category + "/" + postId;
    }

    // 댓글 좋아요
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like/{commentId}")
    public String likeComment(@PathVariable Long commentId,
                              @RequestParam String category,
                              @RequestParam Long postId,
                              Principal principal) {
        SiteUser user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        commentService.toggleLike(commentId, user);

        return "redirect:/posts/user-board/" + category + "/" + postId;
    }

    // 관리자 권한 확인 (간단한 구현)
    private boolean hasAdminRole(Principal principal) {
        SiteUser user = userRepository.findByLoginId(principal.getName())
                .orElse(null);
        return user != null && user.getRole().name().equals("ADMIN");
    }


// ========== 관리자 전용 기능 ==========

    // 댓글 작성자 벤 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ban-author/{commentId}")
    public String banCommentAuthor(@PathVariable Long commentId,
                                   @RequestParam String category,
                                   @RequestParam Long postId) {
        Comment comment = commentService.getComment(commentId);
        userService.banUser(comment.getAuthor().getLoginId());
        return "redirect:/posts/user-board/" + category + "/" + postId;
    }

    // 댓글 삭제 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin-delete/{commentId}")
    public String adminDeleteComment(@PathVariable Long commentId,
                                     @RequestParam String category,
                                     @RequestParam Long postId) {
        commentService.deleteComment(commentId);
        return "redirect:/posts/user-board/" + category + "/" + postId;
    }
}