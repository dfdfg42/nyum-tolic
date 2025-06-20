package com.nyumtolic.nyumtolic.post.comment;

import com.nyumtolic.nyumtolic.post.user.UserPost;
import com.nyumtolic.nyumtolic.post.user.UserPostService;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserPostService userPostService;

    // 댓글 생성
    @Transactional
    public Comment createComment(Long postId, String content, SiteUser author, boolean isAnonymous) {
        UserPost userPost = userPostService.getUserPostById(postId);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserPost(userPost);
        comment.setAuthor(author);
        comment.setAnonymous(isAnonymous);
        comment.setCreateDate(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    // 대댓글 생성
    @Transactional
    public Comment createReply(Long postId, Long parentId, String content, SiteUser author, boolean isAnonymous) {
        UserPost userPost = userPostService.getUserPostById(postId);
        Comment parent = getComment(parentId);

        Comment reply = new Comment();
        reply.setContent(content);
        reply.setUserPost(userPost);
        reply.setAuthor(author);
        reply.setParent(parent);
        reply.setAnonymous(isAnonymous);
        reply.setCreateDate(LocalDateTime.now());

        return commentRepository.save(reply);
    }

    // 댓글 조회
    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
    }

    // 특정 게시글의 댓글 목록 조회 (페이징)
    public Page<Comment> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByUserPostIdAndParentIsNullOrderByCreateDateDesc(postId, pageable);
    }

    // 특정 게시글의 댓글 목록 조회 (전체)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByUserPostIdAndParentIsNullOrderByCreateDateDesc(postId);
    }

    // 특정 댓글의 대댓글 조회
    public List<Comment> getRepliesByParentId(Long parentId) {
        return commentRepository.findByParentIdOrderByCreateDateAsc(parentId);
    }

    // 댓글 수정
    @Transactional
    public Comment updateComment(Long id, String content) {
        Comment comment = getComment(id);
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = getComment(id);
        // 대댓글이 있는 경우 내용만 삭제하고 "삭제된 댓글입니다" 표시
        List<Comment> replies = getRepliesByParentId(id);
        if (!replies.isEmpty()) {
            comment.setContent("삭제된 댓글입니다.");
            comment.setModifyDate(LocalDateTime.now());
            commentRepository.save(comment);
        } else {
            commentRepository.delete(comment);
        }
    }

    // 댓글 좋아요 토글
    @Transactional
    public void toggleLike(Long commentId, SiteUser user) {
        Comment comment = getComment(commentId);

        if (comment.getLiker() == null) {
            comment.setLiker(new java.util.HashSet<>());
        }

        if (comment.getLiker().contains(user)) {
            comment.getLiker().remove(user);
        } else {
            comment.getLiker().add(user);
        }

        commentRepository.save(comment);
    }

    // 댓글 좋아요 수 조회
    public int getLikeCount(Long commentId) {
        Comment comment = getComment(commentId);
        return comment.getLiker() != null ? comment.getLiker().size() : 0;
    }

    // 사용자가 댓글에 좋아요 했는지 확인
    public boolean isLikedByUser(Long commentId, SiteUser user) {
        Comment comment = getComment(commentId);
        return comment.getLiker() != null && comment.getLiker().contains(user);
    }

    // 특정 게시글의 댓글 수 조회
    public Long getCommentCount(Long postId) {
        return commentRepository.countByUserPostId(postId);
    }
}