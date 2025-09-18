package com.example.project_management.service;

import com.example.project_management.dto.*;
import com.example.project_management.model.Role;
import com.example.project_management.model.User;
import com.example.project_management.repository.RoleRepository;
import com.example.project_management.repository.UserRepository;
import com.example.project_management.model.RoleEnum;
import com.example.project_management.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    // ✅ Signup
    public MessageResponse register(SignupRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            return new MessageResponse("❌ Error: Email already in use!");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword())) // Hash password
                .build();

        // Assign default role
        Role role = roleRepo.findByName(RoleEnum.ROLE_USER).orElseThrow();
        user.setRoles(Collections.singleton(role));

        userRepo.save(user);

        return new MessageResponse("✅ User registered successfully!");
    }

    // ✅ Login
    public JwtResponse login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("❌ Invalid email or password"));

        // Compare raw password with BCrypt hash
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("❌ Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        var roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();

        return new JwtResponse("✅ Login successful", token, user.getEmail(), roles);
    }
}
