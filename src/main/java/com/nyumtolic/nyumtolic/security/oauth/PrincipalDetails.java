package com.nyumtolic.nyumtolic.security.oauth;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class PrincipalDetails implements UserDetails, OAuth2User {

    private SiteUser siteUser;
    private Map<String, Object> attributes;

    public PrincipalDetails(SiteUser siteUser) {
        this.siteUser = siteUser;
    }

    public PrincipalDetails(SiteUser siteUser, Map<String, Object> attributes) {
        this.siteUser = siteUser;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> siteUser.getRole().getValue());
        return collection;
    }

    @Override
    public String getPassword() {
        return siteUser.getPassword();
    }

    @Override
    public String getUsername() {
        return siteUser.getLoginId();
    }

    @Override
    public String getName() {
        return siteUser.getNickname();
    }

    @Override
    public boolean isEnabled() {
        return siteUser.getEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public SiteUser getSiteUser() {
        return this.siteUser;
    }
}
