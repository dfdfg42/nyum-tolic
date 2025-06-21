package com.nyumtolic.nyumtolic.post.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserPostRepository extends JpaRepository<UserPost, Long> {

    // 특정 카테고리 게시글 조회 (최신순)
    List<UserPost> findByCategoryOrderByCreateDateDesc(String category);

    // 특정 카테고리 게시글 페이징 조회 (최신순)
    Page<UserPost> findByCategoryOrderByCreateDateDesc(String category, Pageable pageable);

    // 작성자별 게시글 조회 (최신순)
    List<UserPost> findByAuthorIdOrderByCreateDateDesc(Long authorId);

    // 제목으로 검색 (최신순)
    List<UserPost> findByTitleContainingOrderByCreateDateDesc(String keyword);

    // 해시태그로 검색 (최신순)
    List<UserPost> findByHashtagsContainingOrderByCreateDateDesc(String hashtag);

    // 익명 게시글만 조회
    List<UserPost> findByIsAnonymousTrueOrderByCreateDateDesc();

    // 일반 게시글만 조회
    List<UserPost> findByIsAnonymousFalseOrderByCreateDateDesc();

    // 이전글 조회 (같은 카테고리에서 현재 글보다 작성일이 이전인 글 중 가장 최근 글)
    @Query("SELECT p FROM UserPost p WHERE p.category = :category AND p.createDate < :createDate ORDER BY p.createDate DESC")
    List<UserPost> findPreviousPostList(@Param("category") String category, @Param("createDate") LocalDateTime createDate, Pageable pageable);

    // 다음글 조회 (같은 카테고리에서 현재 글보다 작성일이 이후인 글 중 가장 이전 글)
    @Query("SELECT p FROM UserPost p WHERE p.category = :category AND p.createDate > :createDate ORDER BY p.createDate ASC")
    List<UserPost> findNextPostList(@Param("category") String category, @Param("createDate") LocalDateTime createDate, Pageable pageable);

    // 전체 게시판에서의 이전글 조회
    @Query("SELECT p FROM UserPost p WHERE p.createDate < :createDate ORDER BY p.createDate DESC")
    List<UserPost> findPreviousPostInAllList(@Param("createDate") LocalDateTime createDate, Pageable pageable);

    // 전체 게시판에서의 다음글 조회
    @Query("SELECT p FROM UserPost p WHERE p.createDate > :createDate ORDER BY p.createDate ASC")
    List<UserPost> findNextPostInAllList(@Param("createDate") LocalDateTime createDate, Pageable pageable);
}