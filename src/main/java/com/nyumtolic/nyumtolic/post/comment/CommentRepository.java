package com.nyumtolic.nyumtolic.post.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 조회 (최신순, 페이징)
    Page<Comment> findByUserPostIdAndParentIsNullOrderByCreateDateDesc(Long postId, Pageable pageable);

    // 특정 게시글의 모든 댓글 조회 (최신순)
    List<Comment> findByUserPostIdAndParentIsNullOrderByCreateDateDesc(Long postId);

    // 특정 댓글의 대댓글 조회
    List<Comment> findByParentIdOrderByCreateDateAsc(Long parentId);

    // 사용자가 작성한 댓글 조회
    List<Comment> findByAuthorIdOrderByCreateDateDesc(Long authorId);

    // 댓글과 좋아요 수 함께 조회
    @Query("SELECT c, COUNT(l) FROM Comment c LEFT JOIN c.liker l WHERE c.userPost.id = :postId AND c.parent IS NULL GROUP BY c.id ORDER BY c.createDate DESC")
    Page<Object[]> findCommentsWithLikeCountByPostId(@Param("postId") Long postId, Pageable pageable);

    // 특정 게시글의 댓글 수 조회
    Long countByUserPostId(Long postId);
}