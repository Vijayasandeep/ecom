package com.ecommerce.exception;

public class ResourceNotFoundException extends RuntimeException {
    String resource;
    public ResourceNotFoundException(String resource) {
        this.resource = resource;
    }
    public String getResource() {
        return resource;
    }
    public void setResource(String resource) {
        this.resource = resource;
    }
}