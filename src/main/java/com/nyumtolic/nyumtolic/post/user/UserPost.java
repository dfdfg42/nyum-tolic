package com.nyumtolic.nyumtolic.post.user;

import com.nyumtolic.nyumtolic.post.BasePost;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
}
