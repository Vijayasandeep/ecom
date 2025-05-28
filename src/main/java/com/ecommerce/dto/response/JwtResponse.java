package com.ecommerce.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    @Getter
    private String refreshToken;
    @Setter
    @Getter
    private Long id;
    @Setter
    private String username;
    private String email;
    @Getter
    private Collection<? extends GrantedAuthority> authorities;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String email, Collection<? extends GrantedAuthority> authorities) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.authorities = authorities;
    }

    // Getters and Setters
    public String getAccessToken() { return token; }
    public void setAccessToken(String accessToken) { this.token = accessToken; }

    public String getTokenType() { return type; }
    public void setTokenType(String tokenType) { this.type = tokenType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }

}
