package com.nyumtolic.nyumtolic.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// BasePostRepository (공통)
public interface BasePostRepository extends JpaRepository<BasePost, Long> {

    // 타입별 게시글 조회 (ex: NOTICE, USER_BOARD)
    @Query("SELECT p FROM BasePost p WHERE TYPE(p) = :type")
    List<BasePost> findByPostType(@Param("type") Class<? extends BasePost> type);

    // 제목 검색 (모든 타입)
    List<BasePost> findByTitleContaining(String keyword);
}
