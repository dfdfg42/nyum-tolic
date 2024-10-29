package com.nyumtolic.nyumtolic.domain;

import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;  // 리뷰 작성 시간
    private Double rating;            // 별점
    private String content;           // 리뷰 내용
    private Long restaurantId;         // 단순 참조용 레스토랑 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private SiteUser author;          // 작성자

}
