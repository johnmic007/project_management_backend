package com.example.project_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String message;   // ✅ "Login successful"
    private String token;     // ✅ JWT
    private String email;     // ✅ User email
    private List<String> roles; // ✅ Roles
}
