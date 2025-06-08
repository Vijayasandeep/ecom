package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 CustomUserDetailsService.loadUserByUsername called with: " + email);

        // Fetch user from DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        System.out.println("✅ Found user: " + user.getEmail() + " (ID: " + user.getId() + ")");
        System.out.println("👤 User class: " + user.getClass().getName());
        System.out.println("🔐 User authorities: " + user.getAuthorities());

        // CRITICAL: Return your custom User entity directly
        // DO NOT create a new Spring Security User object
        return user;
    }
}