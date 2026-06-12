package com.fitlife.auth.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.auth.dto.*;
import com.fitlife.auth.service.AuthService;
import com.fitlife.auth.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "CĂ¡c API xĂ¡c thá»±c vĂ  Ä‘Äƒng kĂ½")
@SecurityRequirements()
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/login")
    @Operation(summary = "ÄÄƒng nháº­p", description = "XĂ¡c thá»±c tĂ i khoáº£n báº±ng username vĂ  password Ä‘á»ƒ nháº­n JWT token.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(result, "ÄÄƒng nháº­p thĂ nh cĂ´ng"));
    }

    @PostMapping("/register")
    @Operation(summary = "ÄÄƒng kĂ½ tĂ i khoáº£n", description = "Táº¡o má»›i tĂ i khoáº£n ngÆ°á»i dĂ¹ng Ä‘á»ƒ Ä‘Äƒng nháº­p vĂ o há»‡ thá»‘ng FitLife.")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "ÄÄƒng kĂ½ tĂ i khoáº£n thĂ nh cĂ´ng"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "QuĂªn máº­t kháº©u", description = "Gá»­i yĂªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u tá»›i email Ä‘Ă£ Ä‘Äƒng kĂ½.")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String result = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "YĂªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u Ä‘Ă£ Ä‘Æ°á»£c gá»­i thĂ nh cĂ´ng"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Äáº·t láº¡i máº­t kháº©u", description = "XĂ¡c nháº­n email, OTP vĂ  máº­t kháº©u má»›i Ä‘á»ƒ hoĂ n táº¥t quy trĂ¬nh reset password.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Äáº·t láº¡i máº­t kháº©u thĂ nh cĂ´ng"));
    }

    @PostMapping("/google")
    @Operation(summary = "ÄÄƒng nháº­p báº±ng Google", description = "ÄÄƒng nháº­p báº±ng Google ID token Ä‘á»ƒ táº¡o hoáº·c xĂ¡c thá»±c tĂ i khoáº£n FitLife.")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = oAuth2Service.googleLogin(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response, "ÄÄƒng nháº­p báº±ng Google thĂ nh cĂ´ng"));
    }
}