package com.nyumtolic.nyumtolic.security.dto;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getName();
}
