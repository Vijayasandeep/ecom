package com.ecommerce.service;

import com.ecommerce.entity.CustomOAuth2User;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.entity.enums.AuthProvider;
import com.ecommerce.entity.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        return processOAuth2User(userRequest, oauth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(getAuthProvider(registrationId))) {
                throw new OAuth2AuthenticationException(
                        "You're signed up with " + user.getProvider() + " account. Please use your " +
                                user.getProvider() + " account to login."
                );
            }
            user = updateExistingUser(user, oauth2User);
        } else {
            user = registerNewUser(userRequest, oauth2User);
        }

        return new CustomOAuth2User(oauth2User, user);
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        User user = new User();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String providerId = oauth2User.getAttribute("sub") != null ?
                oauth2User.getAttribute("sub").toString() : oauth2User.getAttribute("id").toString();

        user.setProvider(getAuthProvider(registrationId));
        user.setProviderId(providerId);
        user.setEmail(email);
        user.setUsername(email); // Use email as username for OAuth users
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (name != null) {
            String[] names = name.split(" ");
            user.setFirstName(names[0]);
            if (names.length > 1) {
                user.setLastName(names[names.length - 1]);
            }
        }

        // Assign default USER role
        user.setRoles(Collections.singleton(Role.ROLE_USER));

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2User oauth2User) {
        String name = oauth2User.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            String[] names = name.split(" ");
            existingUser.setFirstName(names[0]);
            if (names.length > 1) {
                existingUser.setLastName(names[names.length - 1]);
            }
        }
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    private AuthProvider getAuthProvider(String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return AuthProvider.GOOGLE;
            case "github":
                return AuthProvider.GITHUB;
            default:
                return AuthProvider.LOCAL;
        }
    }
}
