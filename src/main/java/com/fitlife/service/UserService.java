package com.fitlife.service;

import com.fitlife.dto.UserCreationRequest;
import com.fitlife.dto.UserResponse;
import com.fitlife.entity.Role;
import com.fitlife.entity.User;
import com.fitlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        // 1. Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists"); // Sẽ học Global Exception sau
        }
        // 2. Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 2. Map DTO to Entity (Tạm thời lưu password gốc, BCrypt sẽ học ở bài Security)
        User newUser = User.builder()
                .username(request.getUsername())
                .password(hashedPassword)
                .role(request.getRole())
                .status("ACTIVE") // Mặc định tạo user là ACTIVE
                .build();

        // 3. Save to Database
        User savedUser = userRepository.save(newUser);

        // 4. Map Entity back to DTO Response (Hide password)
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .build();
    }
}