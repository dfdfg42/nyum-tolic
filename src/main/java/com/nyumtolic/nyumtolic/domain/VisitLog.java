package com.nyumtolic.nyumtolic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class VisitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Long restaurantId;
    private LocalDateTime visitedAt;  // 방문 시간을 저장하는 필드
}

