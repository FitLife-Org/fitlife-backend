package com.fitlife.auth.service.impl;

import com.fitlife.auth.dto.internal.GoogleTokenPayload;
import com.fitlife.auth.dto.request.*;
import com.fitlife.auth.dto.response.AuthResponse;
import com.fitlife.auth.entity.RefreshToken;
import com.fitlife.auth.mapper.AuthMapper;
import com.fitlife.auth.service.AuthService;
import com.fitlife.auth.service.EmailVerificationService;
import com.fitlife.auth.service.GoogleTokenVerifierService;
import com.fitlife.auth.service.RefreshTokenService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.mail.service.EmailService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.security.JwtService;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.AuthProvider;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.RoleRepository;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_MEMBER_ROLE = "ROLE_MEMBER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

    private final AuthMapper authMapper;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    // =========================================================
    // REGISTER
    // =========================================================

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        Role memberRole = roleRepository.findByCode(DEFAULT_MEMBER_ROLE)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ROLE_NOT_FOUND)
                );

        User user = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(
                        passwordEncoder.encode(request.getPassword())
                )
                .fullName(request.getFullName())
                .phone(normalizeNullable(request.getPhone()))

                // Chưa verify nên chưa cho login.
                .status(UserStatus.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .providerId(null)
                .emailVerified(false)
                .isDeleted(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(memberRole);

        User savedUser = userRepository.save(user);

        createMemberProfileForRegisteredUser(savedUser);

        /*
         * Sinh verification token và gửi email.
         * Không sinh access token ở bước register.
         */
        emailVerificationService
                .createAndSendVerificationToken(savedUser);

        return authMapper.toAuthResponse(
                savedUser,
                null,
                null
        );
    }

    private void validateRegisterRequest(
            RegisterRequest request
    ) {
        validatePasswordConfirmation(
                request.getPassword(),
                request.getConfirmPassword()
        );

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String username =
                request.getUsername()
                        .trim()
                        .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new AppException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }

        String phone =
                normalizeNullable(
                        request.getPhone()
                );

        if (phone != null
                && userRepository.existsByPhone(phone)) {
            throw new AppException(
                    ErrorCode.PHONE_ALREADY_EXISTS
            );
        }
    }

    private void validatePasswordConfirmation(
            String password,
            String confirmPassword
    ) {
        if (password == null
                || confirmPassword == null
                || !password.equals(confirmPassword)) {
            throw new AppException(
                    ErrorCode.PASSWORD_CONFIRM_NOT_MATCH
            );
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier()
                .trim()
                .toLowerCase();

        /*
         * Kiểm tra trước authenticationManager để có thể trả
         * đúng lỗi EMAIL_NOT_VERIFIED thay vì chỉ trả bad credentials.
         */
        User existingUser = findUserByIdentifier(identifier);

        validateUserCanLogin(existingUser);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                identifier,
                                request.getPassword()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        String accessToken =
                jwtService.generateToken(userDetails);

        String refreshToken =
                refreshTokenService.create(user);

        return authMapper.toAuthResponse(
                user,
                accessToken,
                refreshToken
        );
    }

    private User findUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.INVALID_CREDENTIALS
                        )
                );
    }

    private void validateUserCanLogin(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AppException(
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(
                    ErrorCode.EMAIL_NOT_VERIFIED
            );
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(
                    ErrorCode.ACCOUNT_LOCKED
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.ACCOUNT_INACTIVE
            );
        }
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    @Transactional
    public AuthResponse refreshToken(
            RefreshTokenRequest request
    ) {
        RefreshToken refreshTokenEntity =
                refreshTokenService.validate(
                        request.getRefreshToken()
                );

        User user = refreshTokenEntity.getUser();

        validateUserCanLogin(user);

        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        String newAccessToken =
                jwtService.generateToken(userDetails);

        /*
         * MVP: giữ nguyên refresh token hiện tại.
         * Sau này có thể bổ sung refresh-token rotation.
         */
        return authMapper.toAuthResponse(
                user,
                newAccessToken,
                request.getRefreshToken()
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        /*
         * Logout nên idempotent.
         * Service revoke có thể bỏ qua nếu token đã revoked
         * hoặc không còn tồn tại.
         */
        refreshTokenService.revoke(
                request.getRefreshToken()
        );
    }

    @Override
    @Transactional
    public void logoutAll() {
        User currentUser = getCurrentUser();

        refreshTokenService.revokeAllByUserId(
                currentUser.getId()
        );
    }

    // =========================================================
    // VERIFY EMAIL
    // =========================================================

    @Override
    @Transactional
    public void verifyEmail(String token) {
        emailVerificationService.verifyEmail(token);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(
            ResendVerificationEmailRequest request
    ) {
        emailVerificationService.resendVerificationEmail(
                request.getEmail().trim().toLowerCase()
        );
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @Override
    @Transactional
    public void forgotPassword(
            ForgotPasswordRequest request
    ) {
        User user = userRepository
                .findByEmail(
                        request.getEmail()
                                .trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        String otp = generateOtp();

        user.setResetToken(otp);
        user.setResetTokenExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

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
                """.formatted(
                resolveDisplayName(user),
                otp
        );

        emailService.sendSimpleMail(
                user.getEmail(),
                subject,
                content
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Override
    @Transactional
    public void resetPassword(
            ResetPasswordRequest request
    ) {
        validatePasswordConfirmation(
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        User user = userRepository
                .findByEmail(
                        request.getEmail()
                                .trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (user.getResetToken() == null
                || !user.getResetToken()
                .equals(request.getOtp())) {
            throw new AppException(
                    ErrorCode.OTP_INVALID
            );
        }

        if (user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry()
                .isBefore(LocalDateTime.now())) {
            throw new AppException(
                    ErrorCode.OTP_EXPIRED
            );
        }

        if (user.getPasswordHash() != null
                && passwordEncoder.matches(
                request.getNewPassword(),
                user.getPasswordHash()
        )) {
            throw new AppException(
                    ErrorCode.NEW_PASSWORD_SAME_AS_OLD
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        refreshTokenService.revokeAllByUserId(
                user.getId()
        );
    }

    // =========================================================
    // GOOGLE LOGIN
    // =========================================================

    @Override
    @Transactional
    public AuthResponse googleLogin(
            GoogleLoginRequest request
    ) {
        GoogleTokenPayload googlePayload =
                googleTokenVerifierService.verify(
                        request.getIdToken()
                );

        if (!Boolean.TRUE.equals(
                googlePayload.getEmailVerified()
        )) {
            throw new AppException(
                    ErrorCode.EMAIL_NOT_VERIFIED
            );
        }

        User user = userRepository
                .findByAuthProviderAndProviderId(
                        AuthProvider.GOOGLE,
                        googlePayload.getProviderId()
                )
                .orElseGet(() ->
                        findOrCreateGoogleUser(
                                googlePayload
                        )
                );

        validateGoogleUserCanLogin(user);

        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        String accessToken =
                jwtService.generateToken(userDetails);

        String refreshToken =
                refreshTokenService.create(user);

        return authMapper.toAuthResponse(
                user,
                accessToken,
                refreshToken
        );
    }

    private void validateGoogleUserCanLogin(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AppException(
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(
                    ErrorCode.ACCOUNT_LOCKED
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.ACCOUNT_INACTIVE
            );
        }
    }

    private User findOrCreateGoogleUser(
            GoogleTokenPayload googlePayload
    ) {
        return userRepository
                .findByEmail(
                        googlePayload.getEmail()
                                .trim()
                                .toLowerCase()
                )
                .map(existingUser ->
                        linkExistingUserWithGoogle(
                                existingUser,
                                googlePayload
                        )
                )
                .orElseGet(() ->
                        createNewGoogleUser(
                                googlePayload
                        )
                );
    }

    private User linkExistingUserWithGoogle(
            User existingUser,
            GoogleTokenPayload googlePayload
    ) {
        existingUser.setAuthProvider(
                AuthProvider.GOOGLE
        );

        existingUser.setProviderId(
                googlePayload.getProviderId()
        );

        existingUser.setEmailVerified(true);

        /*
         * Nếu user đang PENDING vì chưa verify local,
         * Google verified email có thể kích hoạt tài khoản.
         */
        if (existingUser.getStatus() == UserStatus.PENDING) {
            existingUser.setStatus(UserStatus.ACTIVE);
        }

        if (existingUser.getAvatarUrl() == null
                || existingUser.getAvatarUrl().isBlank()) {
            existingUser.setAvatarUrl(
                    googlePayload.getAvatarUrl()
            );
        }

        User savedUser =
                userRepository.save(existingUser);

        if (hasMemberRole(savedUser)
                && !memberRepository.existsByUserId(
                savedUser.getId()
        )) {
            createMemberProfileForRegisteredUser(
                    savedUser
            );
        }

        return savedUser;
    }

    private User createNewGoogleUser(
            GoogleTokenPayload googlePayload
    ) {
        Role memberRole = roleRepository
                .findByCode(DEFAULT_MEMBER_ROLE)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.ROLE_NOT_FOUND
                        )
                );

        User user = User.builder()
                .username(
                        generateUsernameFromEmail(
                                googlePayload.getEmail()
                        )
                )
                .email(
                        googlePayload.getEmail()
                                .trim()
                                .toLowerCase()
                )
                .passwordHash(null)
                .fullName(
                        resolveGoogleFullName(
                                googlePayload
                        )
                )
                .phone(null)
                .avatarUrl(
                        googlePayload.getAvatarUrl()
                )
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .providerId(
                        googlePayload.getProviderId()
                )
                .emailVerified(true)
                .isDeleted(false)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(memberRole);

        User savedUser = userRepository.save(user);

        createMemberProfileForRegisteredUser(savedUser);

        return savedUser;
    }

    // =========================================================
    // MEMBER PROFILE
    // =========================================================

    private void createMemberProfileForRegisteredUser(
            User user
    ) {
        if (user == null || user.getId() == null) {
            return;
        }

        if (memberRepository.existsByUserId(
                user.getId()
        )) {
            return;
        }

        Member member = Member.builder()
                .user(user)
                .memberCode(
                        generateMemberCode(
                                user.getId()
                        )
                )
                .gender(null)
                .dateOfBirth(null)
                .address(null)
                .emergencyContactName(null)
                .emergencyContactPhone(null)
                .joinDate(LocalDate.now())
                .fitnessGoal(null)
                .healthNote(null)
                .status(MemberStatus.ACTIVE)
                .isDeleted(false)
                .build();

        memberRepository.save(member);
    }

    private String generateMemberCode(Long userId) {
        return "MB" + String.format(
                "%06d",
                userId
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }

        String identifier = authentication.getName();

        return findUserByIdentifier(
                identifier.trim().toLowerCase()
        );
    }

    private String generateUsernameFromEmail(
            String email
    ) {
        String baseUsername = email
                .substring(0, email.indexOf("@"))
                .replaceAll(
                        "[^a-zA-Z0-9_]",
                        ""
                )
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

    private String resolveGoogleFullName(
            GoogleTokenPayload googlePayload
    ) {
        if (googlePayload.getFullName() != null
                && !googlePayload.getFullName()
                .isBlank()) {
            return googlePayload.getFullName();
        }

        return googlePayload.getEmail();
    }

    private boolean hasMemberRole(User user) {
        return user.getRoles() != null
                && user.getRoles()
                .stream()
                .anyMatch(role ->
                        DEFAULT_MEMBER_ROLE.equals(
                                role.getCode()
                        )
                );
    }

    private String generateOtp() {
        int otp = ThreadLocalRandom.current()
                .nextInt(
                        100000,
                        1000000
                );

        return String.valueOf(otp);
    }

    private String resolveDisplayName(User user) {
        if (user.getFullName() == null
                || user.getFullName().isBlank()) {
            return user.getUsername();
        }

        return user.getFullName();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}