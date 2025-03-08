package com.nyumtolic.nyumtolic.post.notice;


import com.nyumtolic.nyumtolic.post.BasePost;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("NOTICE") //BasePost 의 post_type 컬럼 값
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NoticePost extends BasePost {

    private boolean isPinned;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
