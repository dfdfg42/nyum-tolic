package com.nyumtolic.nyumtolic.post.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPostRepository extends JpaRepository<UserPost, Long> {

    // 특정 카테고리 게시글 조회 (최신순)
    List<UserPost> findByCategoryOrderByCreateDateDesc(String category);

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
}