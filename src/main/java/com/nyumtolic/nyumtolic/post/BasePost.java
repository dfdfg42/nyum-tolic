package com.nyumtolic.nyumtolic.post;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorColumn(name = "post_type") //게시글 타입 컬럼 (ex : NOTICE , USER_BOARD)
public abstract class BasePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name  = "author_id")
    private SiteUser author;


    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private Integer viewCount = 0;

    @PrePersist
    protected void onCreate(){
        createDate = LocalDateTime.now();
        modifyDate = createDate;
    }

    @PreUpdate
    protected void onUpdate(){
        modifyDate = LocalDateTime.now();
    }
}
