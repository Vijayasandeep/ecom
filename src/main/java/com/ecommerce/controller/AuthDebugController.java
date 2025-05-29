package com.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class AuthDebugController {

    @GetMapping("/auth-detailed")
    public ResponseEntity<?> getDetailedAuthStatus(HttpServletRequest request) {
        Map<String, Object> debug = new HashMap<>();

        // Check SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        debug.put("securityContextAuthentication", auth != null ? auth.getClass().getSimpleName() : "null");

        if (auth != null) {
            debug.put("authName", auth.getName());
            debug.put("authPrincipal", auth.getPrincipal().getClass().getSimpleName());
            debug.put("authorities", auth.getAuthorities());
            debug.put("isAuthenticated", auth.isAuthenticated());
        }

        // Check Session
        HttpSession session = request.getSession(false);
        debug.put("sessionExists", session != null);
        if (session != null) {
            debug.put("sessionId", session.getId());
            debug.put("sessionCreationTime", session.getCreationTime());
            debug.put("sessionLastAccessTime", session.getLastAccessedTime());
        }

        // Check if OAuth2
        if (auth instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            debug.put("oauthRegistrationId", oauthToken.getAuthorizedClientRegistrationId());
            debug.put("oauthPrincipal", oauthToken.getPrincipal().getAttributes());
        }

        return ResponseEntity.ok(debug);
    }

    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(HttpServletRequest request) {
        Map<String, Object> sessionInfo = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionInfo.put("sessionId", session.getId());
            sessionInfo.put("isNew", session.isNew());
            sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());

            // List all session attributes
            Map<String, Object> attributes = new HashMap<>();
            session.getAttributeNames().asIterator().forEachRemaining(name ->
                    attributes.put(name, session.getAttribute(name).getClass().getSimpleName())
            );
            sessionInfo.put("sessionAttributes", attributes);
        } else {
            sessionInfo.put("message", "No session found");
        }

        return ResponseEntity.ok(sessionInfo);
    }
}