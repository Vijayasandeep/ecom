package com.ecommerce.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MessageResponse {
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageResponse(String message) {
        this.message = message;
    }

}
