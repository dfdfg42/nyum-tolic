package com.nyumtolic.nyumtolic.security.oauth;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getName();
}
