package com.nyumtolic.nyumtolic.review;



import com.nyumtolic.nyumtolic.domain.Restaurant;
import com.nyumtolic.nyumtolic.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    @ManyToOne
    private SiteUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToMany
    Set<SiteUser> voter;

}
