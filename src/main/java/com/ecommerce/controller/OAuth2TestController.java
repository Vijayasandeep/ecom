package com.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2TestController {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    /**
     * Test if OAuth2 configuration is loaded correctly
     */
    @GetMapping("/config-test")
    public ResponseEntity<?> testOAuth2Config() {
        try {
            ClientRegistration googleRegistration =
                    clientRegistrationRepository.findByRegistrationId("google");

            Map<String, Object> config = new HashMap<>();
            config.put("clientId", googleRegistration.getClientId().substring(0, 10) + "...");
            config.put("redirectUri", googleRegistration.getRedirectUri());
            config.put("scopes", googleRegistration.getScopes());
            config.put("authorizationUri", googleRegistration.getProviderDetails().getAuthorizationUri());

            return ResponseEntity.ok(config);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "OAuth2 configuration not found: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Test current authentication status
     */
    @GetMapping("/auth-status")
    public ResponseEntity<?> getAuthStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("authenticated", false);
            response.put("message", "No authentication found");
        } else {
            response.put("authenticated", true);
            response.put("type", authentication.getClass().getSimpleName());
            response.put("name", authentication.getName());
            response.put("authorities", authentication.getAuthorities());

            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                response.put("oauth2Attributes", oauth2User.getAttributes());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user information (for OAuth2 authenticated users)
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getCurrentUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("message", "Not authenticated"));
        }

        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", oauth2User.getAttribute("name"));
            userInfo.put("email", oauth2User.getAttribute("email"));
            userInfo.put("picture", oauth2User.getAttribute("picture"));
            userInfo.put("sub", oauth2User.getAttribute("sub"));

            return ResponseEntity.ok(userInfo);
        }

        return ResponseEntity.ok(Map.of("message", "Not OAuth2 authenticated"));
    }

    /**
     * Simple endpoint to test after authentication
     */
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Access granted!",
                "user", authentication.getName(),
                "timestamp", System.currentTimeMillis()
        ));
    }
    @GetMapping("/login-success")
    public String loginSuccess() {
        return "login-success.html";  // return login-success.html
    }
}