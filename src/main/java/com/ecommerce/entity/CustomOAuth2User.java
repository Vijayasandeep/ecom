package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class CustomOAuth2User implements OAuth2User, OidcUser {

    private OAuth2User oauth2User;
    private OidcUser oidcUser;
    private User user;

    // Constructor for regular OAuth2
    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        this.oauth2User = oauth2User;
        this.user = user;
    }

    // Constructor for OIDC
    public CustomOAuth2User(OidcUser oidcUser, User user) {
        this.oidcUser = oidcUser;
        this.oauth2User = oidcUser; // OidcUser extends OAuth2User
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

    // OIDC User methods
    @Override
    public Map<String, Object> getClaims() {
        return oidcUser != null ? oidcUser.getClaims() : getAttributes();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser != null ? oidcUser.getUserInfo() : null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser != null ? oidcUser.getIdToken() : null;
    }

    // Custom methods
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

    public OidcUser getOidcUser() {
        return oidcUser;
    }

    public void setOidcUser(OidcUser oidcUser) {
        this.oidcUser = oidcUser;
    }

    // Convenience methods to get user information
    public String getEmail() {
        if (user != null) {
            return user.getEmail();
        }
        if (oidcUser != null) {
            return oidcUser.getEmail();
        }
        return oauth2User.getAttribute("email");
    }

    public String getFirstName() {
        if (user != null) {
            return user.getFirstName();
        }
        if (oidcUser != null) {
            return oidcUser.getGivenName();
        }
        return oauth2User.getAttribute("given_name");
    }

    public String getLastName() {
        if (user != null) {
            return user.getLastName();
        }
        if (oidcUser != null) {
            return oidcUser.getFamilyName();
        }
        return oauth2User.getAttribute("family_name");
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    // Check if this is an OIDC user
    public boolean isOidcUser() {
        return oidcUser != null;
    }
}