package com.ecommerce.controller;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.SignUpRequest;
import com.ecommerce.dto.response.JwtResponse;
import com.ecommerce.dto.response.MessageResponse;
import com.ecommerce.dto.response.TokenRefreshRequest;
import com.ecommerce.dto.response.TokenRefreshResponse;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.Role;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("LoginRequest: " + loginRequest.getEmail() + " / " + loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            String username = authentication.getName();
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User userPrincipal = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwt = jwtUtil.generateToken(userPrincipal);
            String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    refreshToken,
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getAuthorities()
            ));
        } catch (Exception e) {
            e.printStackTrace();  // This will help debug
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid credentials"));
        }

    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail((signUpRequest.getUsername()))) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            // Default role (e.g., USER)
            roles.add(Role.ROLE_USER); // Directly use the enum
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) { // Case-insensitive check
                    case "admin":
                        roles.add(Role.ROLE_ADMIN); // Direct enum assignment
                        break;
                    case "seller":
                        roles.add(Role.ROLE_SELLER); // Direct enum assignment
                        break;
                    default:
                        roles.add(Role.ROLE_USER); // Default role
                }
            });
        }

        user.setRoles(Set.of(Role.ROLE_USER));// Make sure `user.setRoles()` accepts `Set<Role>`
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (jwtUtil.validateToken(requestRefreshToken)) {
            String username = jwtUtil.extractUsername(requestRefreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Refresh token is not valid!"));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }
}