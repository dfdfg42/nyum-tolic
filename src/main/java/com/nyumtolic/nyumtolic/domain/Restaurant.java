package com.nyumtolic.nyumtolic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Category> categories;

    @Column(name = "rating")
    private Double rating;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menu;

    @Column(name = "description")
    private String description;

    @Column(name = "travel_time")
    private Integer travelTime;
}
