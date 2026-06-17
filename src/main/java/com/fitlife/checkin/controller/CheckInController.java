//package com.fitlife.checkin.controller;
//
//import com.fitlife.checkin.dto.CheckInResponse;
//import com.fitlife.checkin.service.CheckInService;
//import com.fitlife.common.response.ApiResponse;
//import com.fitlife.auth.entity.User;
//import com.fitlife.auth.repository.UserRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/checkin")
//@RequiredArgsConstructor
//@Tag(name = "Check-in Management", description = "Xá»­ lĂ½ check-in táº¡i quáº§y hoáº·c tá»± check-in cá»§a há»™i viĂªn")
//public class CheckInController {
//
//    private final CheckInService checkInService;
//    private final UserRepository userRepository;
//
//    /**
//     * 1. Staff/Admin: Staff scan card/qr of member
//     */
//    @PostMapping("/{memberId}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
//    @Operation(summary = "Check-in cho há»™i viĂªn bá»Ÿi nhĂ¢n viĂªn", description = "NhĂ¢n viĂªn/quáº£n lĂ½ quĂ©t hoáº·c nháº­p memberId Ä‘á»ƒ xĂ¡c nháº­n check-in táº¡i quáº§y.")
//    public ResponseEntity<ApiResponse<CheckInResponse>> staffProcessCheckIn(
//            @Parameter(description = "ID há»™i viĂªn cáº§n check-in", example = "101")
//            @PathVariable Long memberId,
//            Authentication authentication) {
//
//        // Get information about the Staff performing the operation
//        User staffUser = userRepository.findByUsername(authentication.getName())
//                .orElseThrow(() -> new RuntimeException("TĂ i khoáº£n nhĂ¢n viĂªn khĂ´ng há»£p lá»‡"));
//
//        // ANTI-FRAUD LOGIC: Staff/Admins are not allowed to check-in for themselves
//        if (staffUser.getMember() != null && staffUser.getMember().getId().equals(memberId)) {
//            throw new RuntimeException("Lá»–I GIAN Láº¬N: NhĂ¢n viĂªn hoáº·c Quáº£n lĂ½ khĂ´ng thá»ƒ tá»± check-in cho chĂ­nh mĂ¬nh táº¡i quáº§y!");
//        }
//
//        CheckInResponse result = checkInService.processCheckIn(memberId, staffUser.getUsername());
//        return ResponseEntity.ok(ApiResponse.success(result, "Check-in xá»­ lĂ½ thĂ nh cĂ´ng bá»Ÿi nhĂ¢n viĂªn"));
//    }
//
//    /**
//     * 2. Stream Self-Service: Members automatically open the App to scan the code at the door
//     */
//    @PostMapping("/me")
//    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
//    @Operation(summary = "Tá»± check-in cá»§a há»™i viĂªn", description = "Há»™i viĂªn tá»± thá»±c hiá»‡n check-in báº±ng tĂ i khoáº£n cá»§a mĂ¬nh.")
//    public ResponseEntity<ApiResponse<CheckInResponse>> memberSelfCheckIn(Authentication authentication) {
//
//        User user = userRepository.findByUsername(authentication.getName())
//                .orElseThrow(() -> new RuntimeException("TĂ i khoáº£n khĂ´ng há»£p lá»‡"));
//
//        if (user.getMember() == null) {
//            throw new RuntimeException("TĂ i khoáº£n nĂ y chÆ°a cĂ³ há»“ sÆ¡ há»™i viĂªn!");
//        }
//
//        CheckInResponse result = checkInService.processCheckIn(user.getMember().getId(), user.getUsername());
//        return ResponseEntity.ok(ApiResponse.success(result, "Há»™i viĂªn tá»± check-in thĂ nh cĂ´ng"));
//    }
//}