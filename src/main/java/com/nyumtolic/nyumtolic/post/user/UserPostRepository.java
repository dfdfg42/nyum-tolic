package com.nyumtolic.nyumtolic.post.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPostRepository extends JpaRepository<UserPost, Long> {
    // 추후 검색이나 정렬 등 추가 쿼리 메서드 작성 가능
}