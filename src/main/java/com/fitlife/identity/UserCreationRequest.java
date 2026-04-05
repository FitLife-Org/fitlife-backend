package com.fitlife.identity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreationRequest {
    private String username;
    private String password;
    private String role;
}
