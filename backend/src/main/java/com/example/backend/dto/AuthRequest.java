package com.example.backend.dto;

import com.example.backend.entity.Role;
import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;

}
