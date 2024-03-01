package com.nyumtolic.nyumtolic.domain;

import jakarta.persistence.*;


@Entity
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;


    @Column(name = "price")
    private Double price;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

}
