package com.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean isDefault;

    // Constructors, getters, setters
}