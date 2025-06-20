package com.nyumtolic.nyumtolic.post.user;

import com.nyumtolic.nyumtolic.post.BasePost;
import com.nyumtolic.nyumtolic.post.comment.Comment;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue("USER_BOARD")
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class UserPost extends BasePost {
    private boolean isAnonymous;
    private String category;
    private String hashtags;

    // 좋아요 기능 추가
    @ManyToMany
    @JoinTable(
            name = "user_post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<SiteUser> liker;

    // 댓글 관계 추가 - 게시글 삭제 시 댓글도 함께 삭제
    @OneToMany(mappedBy = "userPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // BasePost의 viewCount 필드를 사용하므로 별도 선언 불필요
}