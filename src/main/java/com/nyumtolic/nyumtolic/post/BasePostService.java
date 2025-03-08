package com.nyumtolic.nyumtolic.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasePostService {

    private final BasePostRepository basePostRepository;

    // 게시글 생성 (타입에 따라 하위 클래스로 저장)
    @Transactional
    public <T extends BasePost> T createPost(T post) {
        return basePostRepository.save(post);
    }

    // 게시글 타입별 목록 조회
    public List<BasePost> getPostsByType(Class<? extends BasePost> type) {
        return basePostRepository.findByPostType(type);
    }
}