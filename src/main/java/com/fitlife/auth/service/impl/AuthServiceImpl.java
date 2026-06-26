package com.fitlife.auth.service.impl;

import com.fitlife.auth.dto.internal.GoogleTokenPayload;
import com.fitlife.auth.dto.request.*;
import com.fitlife.auth.dto.response.AuthResponse;
import com.fitlife.auth.mapper.AuthMapper;
import com.fitlife.auth.service.AuthService;
import com.fitlife.auth.service.GoogleTokenVerifierService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.role.entity.Role;
import com.fitlife.role.repository.RoleRepository;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.security.JwtService;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.AuthProvider;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.mapper.UserMapper;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.mail.service.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_MEMBER_ROLE = "ROLE_MEMBER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthMapper authMapper;
    private final MemberRepository memberRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (request.getPhone() != null
                && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Role memberRole = roleRepository.findByCode(DEFAULT_MEMBER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .providerId(null)
                .emailVerified(false)
                .isDeleted(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(memberRole);

        User savedUser = userRepository.save(user);

        createMemberProfileForRegisteredUser(savedUser);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String accessToken = jwtService.generateToken(userDetails);

        return authMapper.toAuthResponse(savedUser, accessToken);
    }

    private void createMemberProfileForRegisteredUser(User user) {
        if (memberRepository.existsByUserId(user.getId())) {
            return;
        }

        Member member = Member.builder()
                .user(user)
                .memberCode(generateMemberCode(user.getId()))
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .gender(null)
                .dateOfBirth(null)
                .avatarUrl(user.getAvatarUrl())
                .heightCm(null)
                .weightKg(null)
                .bmi(null)
                .fitnessGoal(null)
                .status(MemberStatus.ACTIVE)
                .isDeleted(false)
                .build();

        memberRepository.save(member);
    }

    private String generateMemberCode(Long userId) {
        return "MB" + String.format("%06d", userId);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        identifier,
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtService.generateToken(userDetails);

        return authMapper.toAuthResponse(user, accessToken);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = generateOtp();

        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        String subject = "FitLife Password Reset OTP";

        String content = """
            Hello %s,
            
            We received a request to reset your FitLife password.
            
            Your OTP is:
            %s
            
            This OTP will expire in 5 minutes.
            
            If you did not request this, please ignore this email.
            
            FitLife Team
            """.formatted(user.getFullName(), otp);

        emailService.sendSimpleMail(user.getEmail(), subject, content);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        if (user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (user.getPasswordHash() != null
                && passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleTokenPayload googlePayload = googleTokenVerifierService.verify(request.getIdToken());

        User user = userRepository
                .findByAuthProviderAndProviderId(AuthProvider.GOOGLE, googlePayload.getProviderId())
                .orElseGet(() -> findOrCreateGoogleUser(googlePayload));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);

        return authMapper.toAuthResponse(user, accessToken);
    }

    private User findOrCreateGoogleUser(GoogleTokenPayload googlePayload) {
        return userRepository.findByEmail(googlePayload.getEmail())
                .map(existingUser -> linkExistingUserWithGoogle(existingUser, googlePayload))
                .orElseGet(() -> createNewGoogleUser(googlePayload));
    }

    private User linkExistingUserWithGoogle(
            User existingUser,
            GoogleTokenPayload googlePayload
    ) {
        existingUser.setAuthProvider(AuthProvider.GOOGLE);
        existingUser.setProviderId(googlePayload.getProviderId());
        existingUser.setEmailVerified(Boolean.TRUE.equals(googlePayload.getEmailVerified()));

        if (existingUser.getAvatarUrl() == null || existingUser.getAvatarUrl().isBlank()) {
            existingUser.setAvatarUrl(googlePayload.getAvatarUrl());
        }

        User savedUser = userRepository.save(existingUser);

        if (hasMemberRole(savedUser) && !memberRepository.existsByUserId(savedUser.getId())) {
            createMemberProfileForRegisteredUser(savedUser);
        }

        return savedUser;
    }

    private User createNewGoogleUser(GoogleTokenPayload googlePayload) {
        Role memberRole = roleRepository.findByCode(DEFAULT_MEMBER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .username(generateUsernameFromEmail(googlePayload.getEmail()))
                .email(googlePayload.getEmail())
                .passwordHash(null)
                .fullName(resolveGoogleFullName(googlePayload))
                .phone(null)
                .avatarUrl(googlePayload.getAvatarUrl())
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .providerId(googlePayload.getProviderId())
                .emailVerified(Boolean.TRUE.equals(googlePayload.getEmailVerified()))
                .isDeleted(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(memberRole);

        User savedUser = userRepository.save(user);

        createMemberProfileForRegisteredUser(savedUser);

        return savedUser;
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf("@"))
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase();

        if (baseUsername.isBlank()) {
            baseUsername = "google_user";
        }

        String username = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    private String resolveGoogleFullName(GoogleTokenPayload googlePayload) {
        if (googlePayload.getFullName() != null && !googlePayload.getFullName().isBlank()) {
            return googlePayload.getFullName();
        }

        return googlePayload.getEmail();
    }

    private boolean hasMemberRole(User user) {
        return user.getRoles()
                .stream()
                .anyMatch(role -> DEFAULT_MEMBER_ROLE.equals(role.getCode()));
    }

    private String generateOtp() {
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(otp);
    }
}