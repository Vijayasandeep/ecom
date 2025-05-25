package com.ecommerce.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private OAuth2User oauth2User;
    private User user;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        this.oauth2User = oauth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("name");
    }

    // This is the missing method that was causing the compilation error
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OAuth2User getOauth2User() {
        return oauth2User;
    }

    public void setOauth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    // Convenience methods to get user information
    public String getEmail() {
        return user != null ? user.getEmail() : oauth2User.getAttribute("email");
    }

    public String getFirstName() {
        return user != null ? user.getFirstName() : oauth2User.getAttribute("given_name");
    }

    public String getLastName() {
        return user != null ? user.getLastName() : oauth2User.getAttribute("family_name");
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}