package com.nyumtolic.nyumtolic.post.user;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPostService {

    private final UserPostRepository userPostRepository;

    // 모든 유저 게시글 조회 (최신순)
    public List<UserPost> getUserPosts() {
        return userPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createDate"));
    }

    // 모든 유저 게시글 페이징 조회
    public Page<UserPost> getUserPostsPaged(Pageable pageable) {
        return userPostRepository.findAll(pageable);
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

    // 조회수 증가
    @Transactional
    public void incrementViewCount(Long id) {
        UserPost userPost = getUserPostById(id);
        userPost.setViewCount((userPost.getViewCount() != null ? userPost.getViewCount() : 0) + 1);
        userPostRepository.save(userPost);
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

    // 특정 카테고리의 유저 게시글 페이징 조회
    public Page<UserPost> getPostsByCategoryPaged(String category, Pageable pageable) {
        return userPostRepository.findByCategoryOrderByCreateDateDesc(category, pageable);
    }

    // 제목으로 검색
    public List<UserPost> searchByTitle(String keyword) {
        return userPostRepository.findByTitleContainingOrderByCreateDateDesc(keyword);
    }

    // 해시태그로 검색
    public List<UserPost> searchByHashtag(String hashtag) {
        return userPostRepository.findByHashtagsContainingOrderByCreateDateDesc(hashtag);
    }

    // 좋아요 토글
    @Transactional
    public void toggleLike(Long postId, SiteUser user) {
        UserPost userPost = getUserPostById(postId);

        if (userPost.getLiker() == null) {
            userPost.setLiker(new java.util.HashSet<>());
        }

        if (userPost.getLiker().contains(user)) {
            userPost.getLiker().remove(user);
        } else {
            userPost.getLiker().add(user);
        }

        userPostRepository.save(userPost);
    }

    // 좋아요 수 조회
    public int getLikeCount(Long postId) {
        UserPost userPost = getUserPostById(postId);
        return userPost.getLiker() != null ? userPost.getLiker().size() : 0;
    }

    // 사용자가 좋아요 했는지 확인
    public boolean isLikedByUser(Long postId, SiteUser user) {
        UserPost userPost = getUserPostById(postId);
        return userPost.getLiker() != null && userPost.getLiker().contains(user);
    }

    // 이전글 조회
    public Optional<UserPost> getPreviousPost(String category, UserPost currentPost) {
        Pageable pageable = PageRequest.of(0, 1); // 첫 번째 결과 1개만
        List<UserPost> posts;

        if (category == null || "all".equals(category)) {
            posts = userPostRepository.findPreviousPostInAllList(currentPost.getCreateDate(), pageable);
        } else {
            posts = userPostRepository.findPreviousPostList(category, currentPost.getCreateDate(), pageable);
        }

        return posts.isEmpty() ? Optional.empty() : Optional.of(posts.get(0));
    }

    // 다음글 조회
    public Optional<UserPost> getNextPost(String category, UserPost currentPost) {
        Pageable pageable = PageRequest.of(0, 1); // 첫 번째 결과 1개만
        List<UserPost> posts;

        if (category == null || "all".equals(category)) {
            posts = userPostRepository.findNextPostInAllList(currentPost.getCreateDate(), pageable);
        } else {
            posts = userPostRepository.findNextPostList(category, currentPost.getCreateDate(), pageable);
        }

        return posts.isEmpty() ? Optional.empty() : Optional.of(posts.get(0));
    }

    // 이전글/다음글 네비게이션 정보를 담는 내부 클래스
    public static class PostNavigation {
        private final UserPost previousPost;
        private final UserPost nextPost;

        public PostNavigation(UserPost previousPost, UserPost nextPost) {
            this.previousPost = previousPost;
            this.nextPost = nextPost;
        }

        public UserPost getPreviousPost() {
            return previousPost;
        }

        public UserPost getNextPost() {
            return nextPost;
        }
    }

    // 이전글/다음글 네비게이션 정보 조회
    public PostNavigation getPostNavigation(String category, UserPost currentPost) {
        Optional<UserPost> previousPost = getPreviousPost(category, currentPost);
        Optional<UserPost> nextPost = getNextPost(category, currentPost);

        return new PostNavigation(
                previousPost.orElse(null),
                nextPost.orElse(null)
        );
    }
}