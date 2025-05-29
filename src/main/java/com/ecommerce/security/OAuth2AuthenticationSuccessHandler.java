package com.ecommerce.security;

import com.ecommerce.entity.CustomOAuth2User;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        System.out.println("=== OAuth2AuthenticationSuccessHandler CALLED ===");
        System.out.println("Authentication class: " + authentication.getClass().getSimpleName());
        System.out.println("Authentication name: " + authentication.getName());
        System.out.println("Is authenticated: " + authentication.isAuthenticated());
        System.out.println("Principal class: " + authentication.getPrincipal().getClass().getSimpleName());

        try {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            System.out.println("CustomOAuth2User email: " + oauth2User.getEmail());
            System.out.println("User from database ID: " + oauth2User.getUser().getId());

            System.out.println("Generating JWT tokens...");
            String token = jwtUtil.generateToken(oauth2User.getUser());
            String refreshToken = jwtUtil.generateRefreshToken(oauth2User.getUser());
            System.out.println("✅ JWT tokens generated successfully");
            System.out.println("Token length: " + token.length());

            String targetUrl = UriComponentsBuilder.fromUriString("/debug/auth-detailed")
//                    .queryParam("token", token)
//                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            System.out.println("Redirecting to: " + targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            System.out.println("✅ Redirect completed successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR in OAuth2AuthenticationSuccessHandler: " + e.getMessage());
            e.printStackTrace();

            // For debugging, redirect to a simple success page instead of failing
            System.out.println("Redirecting to debug endpoint due to error...");
            getRedirectStrategy().sendRedirect(request, response, "/debug/auth-detailed");
        }
    }
}