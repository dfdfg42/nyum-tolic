package com.nyumtolic.nyumtolic.post.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPostService {

    private final UserPostRepository userPostRepository;

    public List<UserPost> getUserPosts() {
        return userPostRepository.findAll();
    }

    @Transactional
    public UserPost createUserPost(UserPost userPost) {
        return userPostRepository.save(userPost);
    }
}