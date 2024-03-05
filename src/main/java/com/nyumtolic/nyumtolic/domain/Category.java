package com.nyumtolic.nyumtolic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 메인 카테고리 여부를 나타내는 필드 추가
    @Column(name = "is_main_category")
    private boolean isMainCategory;

    @ManyToMany(mappedBy = "categories")
    private List<Restaurant> restaurants;

}
