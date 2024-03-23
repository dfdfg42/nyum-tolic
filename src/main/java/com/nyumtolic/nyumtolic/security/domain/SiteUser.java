package com.nyumtolic.nyumtolic.security.domain;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String nickname;

    private String password;

    @Column(unique = true)
    private String email;

    private String provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
