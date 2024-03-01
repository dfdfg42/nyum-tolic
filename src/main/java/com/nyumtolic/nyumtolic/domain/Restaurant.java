package com.nyumtolic.nyumtolic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo")
    private String photo;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToMany
    private Set<Category> categories;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "menu")
    private String menu;

    @Column(name = "description")
    private String description;
}
