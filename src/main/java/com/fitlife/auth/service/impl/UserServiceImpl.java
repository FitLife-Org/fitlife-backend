package com.fitlife.auth.service.impl;

import com.fitlife.auth.dto.UserCreationRequest;
import com.fitlife.auth.dto.UserResponse;
import com.fitlife.auth.entity.User;
import com.fitlife.auth.mapper.UserMapper;
import com.fitlife.auth.repository.UserRepository;
import com.fitlife.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        // 1. Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists"); // Sáº½ há»c Global Exception sau
        }
        // 2. Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 2. Map DTO to Entity (Táº¡m thá»i lÆ°u password gá»‘c, BCrypt sáº½ há»c á»Ÿ bĂ i Security)
        User newUser = userMapper.toEntity(request, hashedPassword);
        newUser.setStatus("ACTIVE"); // Máº·c Ä‘á»‹nh táº¡o user lĂ  ACTIVE

        // 3. Save to Database
        User savedUser = userRepository.save(newUser);
        String roleCode = request.getRole();
        if (roleCode != null && !roleCode.startsWith("ROLE_")) {
            roleCode = "ROLE_" + roleCode;
        }
        if (roleCode != null && !roleCode.isBlank()) {
            userRepository.assignRoleToUser(savedUser.getId(), roleCode);
            savedUser.setRole(roleCode);
        }

        // 4. Map Entity back to DTO Response (Hide password)
        return userMapper.toResponse(savedUser);
    }
}