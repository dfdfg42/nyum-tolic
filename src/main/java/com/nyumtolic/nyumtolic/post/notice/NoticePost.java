package com.nyumtolic.nyumtolic.post.notice;


import com.nyumtolic.nyumtolic.post.BasePost;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("NOTICE") //BasePost 의 post_type 컬럼 값
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NoticePost extends BasePost {

    private boolean isPinned;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
}
