package com.nyumtolic.nyumtolic.api.domain;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import lombok.Data;

@Data
public class AuthorDTO {
    private Long id;
    private String username;
    private String email;

    public AuthorDTO(SiteUser author) {
        this.id = author.getId();
        this.username = author.getLoginId();
        this.email = author.getEmail();
    }
}