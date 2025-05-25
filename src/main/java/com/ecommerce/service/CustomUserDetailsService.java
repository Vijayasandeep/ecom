package com.ecommerce.service;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from DB (replace with your logic)
        return User.builder()
                .username(username)
                .password("{noop}password") // {noop} for plaintext (use BCrypt in prod)
                .roles("USER")
                .build();
    }
}

