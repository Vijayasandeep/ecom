package com.ecommerce.security;

import com.ecommerce.service.CustomUserDetailsService;
import com.ecommerce.service.impl.UserDetailsServiceImpl;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
//    private UserDetailsServiceImpl userDetailsService;
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().equals("/api/auth/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        System.out.println("🔍 JWT Filter - Authorization header: " + requestTokenHeader);

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
                System.out.println("📧 Extracted username from JWT: " + username);
            } catch (Exception e) {
                System.err.println("❌ Unable to get JWT Token: " + e.getMessage());
                logger.error("Unable to get JWT Token", e);
            }
        }

        // Validate token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("🔐 Loading user details for: " + username);
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            System.out.println("👤 Loaded UserDetails class: " + userDetails.getClass().getName());
            System.out.println("🔑 UserDetails authorities: " + userDetails.getAuthorities());

            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                System.out.println("✅ JWT token is valid");
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("🔒 Authentication set in SecurityContext");
                System.out.println("🎯 Principal class: " + authToken.getPrincipal().getClass().getName());
            } else {
                System.out.println("❌ JWT token validation failed");
            }
        }

        filterChain.doFilter(request, response);
    }
}