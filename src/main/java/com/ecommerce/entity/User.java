package com.ecommerce.entity;

import com.ecommerce.entity.audit.BaseEntity;
import com.ecommerce.entity.enums.AuthProvider;
import com.ecommerce.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone")
})
@AllArgsConstructor
//@Builder
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonIgnore
    private String password;

    @Column(name = "first_name")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(unique = true)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    // Default constructor (required by JPA)
    public User() {
        super();
    }

    // Custom constructors
    public User(String username, String email, String password) {
        super();
        this.email = email;
        this.password = password;
        // If username is different from email, you might want to handle it differently
        // For now, assuming email is the username
    }

    // Additional constructor for OAuth2 users
    public User(String email, String firstName, String lastName, AuthProvider provider, String providerId) {
        super();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.provider = provider;
        this.providerId = providerId;
        this.emailVerified = true; // OAuth users are typically pre-verified
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("🔑 User.getAuthorities() called for: " + email);
        System.out.println("📋 User role: " + role);

        if (role == null) {
            System.out.println("⚠️ Role is null, returning default ROLE_USER");
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        String authority = role.name(); // This should be like "ROLE_ADMIN", "ROLE_USER", etc.
        System.out.println("✅ Returning authority: " + authority);
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive != null ? isActive : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null ? isActive : true;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

//    // Additional getter methods that might be needed
//    public AuthProvider getProvider() {
//        return provider;
//    }
//
//    // Custom setter methods (fixing the ones with issues)
//    public void setProvider(AuthProvider provider) {
//        this.provider = provider;
//    }

    public void setUsername(String email) {
        this.email = email; // Since username is email in this case
    }

    public void setEnabled(boolean enabled) {
        this.isActive = enabled;
    }

    public void setRoles(Set<Role> roles) {
        if (roles != null && !roles.isEmpty()) {
            this.role = roles.iterator().next(); // Take the first role since we have single role
        }
    }

    // Convenience methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.toString();
    }

    public boolean isOAuth2User() {
        return provider != null && provider != AuthProvider.LOCAL;
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate(); // Call parent's onCreate if it exists
        if (isActive == null) {
            isActive = true;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
        if (phoneVerified == null) {
            phoneVerified = false;
        }
    }

    // Explicit Getter Methods
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    // Explicit Setter Methods
    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}