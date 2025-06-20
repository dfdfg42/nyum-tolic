package com.nyumtolic.nyumtolic.post.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPostService {

    private final UserPostRepository userPostRepository;

    // 모든 유저 게시글 조회 (최신순)
    public List<UserPost> getUserPosts() {
        return userPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createDate"));
    }

    // 유저 게시글 생성
    @Transactional
    public UserPost createUserPost(UserPost userPost) {
        return userPostRepository.save(userPost);
    }

    // ID로 유저 게시글 상세 조회
    public UserPost getUserPostById(Long id) {
        return userPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    // 유저 게시글 수정
    @Transactional
    public UserPost updateUserPost(Long id, UserPost updatedPost) {
        UserPost userPost = getUserPostById(id);

        userPost.setTitle(updatedPost.getTitle());
        userPost.setContent(updatedPost.getContent());
        userPost.setAnonymous(updatedPost.isAnonymous());
        userPost.setHashtags(updatedPost.getHashtags());

        return userPostRepository.save(userPost);
    }

    // 유저 게시글 삭제
    @Transactional
    public void deleteUserPost(Long id) {
        UserPost userPost = getUserPostById(id);
        userPostRepository.delete(userPost);
    }

    // 특정 카테고리의 유저 게시글 조회 (최신순)
    public List<UserPost> getPostsByCategory(String category) {
        return userPostRepository.findByCategoryOrderByCreateDateDesc(category);
    }

    // 제목으로 검색
    public List<UserPost> searchByTitle(String keyword) {
        return userPostRepository.findByTitleContainingOrderByCreateDateDesc(keyword);
    }

    // 해시태그로 검색
    public List<UserPost> searchByHashtag(String hashtag) {
        return userPostRepository.findByHashtagsContainingOrderByCreateDateDesc(hashtag);
    }
}