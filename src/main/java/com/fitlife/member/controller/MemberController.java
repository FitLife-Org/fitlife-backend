package com.fitlife.member.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import com.fitlife.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "Quáº£n lĂ½ há»“ sÆ¡ há»™i viĂªn vĂ  thao tĂ¡c dĂ nh cho admin/staff")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Táº¡o há»“ sÆ¡ há»™i viĂªn", description = "Táº¡o má»›i há»“ sÆ¡ há»™i viĂªn cho tĂ i khoáº£n Ä‘Ă£ cĂ³ sáºµn trong há»‡ thá»‘ng.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Member created successfully"));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cáº­p nháº­t avatar há»™i viĂªn", description = "Upload áº£nh Ä‘áº¡i diá»‡n má»›i cho há»™i viĂªn hiá»‡n táº¡i.")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @Parameter(description = "Tá»‡p áº£nh cáº§n upload")
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {
        String avatarUrl = memberService.updateAvatar(principal.getName(), file);
        return ResponseEntity.ok(ApiResponse.success(avatarUrl, "Cáº­p nháº­t áº£nh Ä‘áº¡i diá»‡n thĂ nh cĂ´ng"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Láº¥y danh sĂ¡ch há»™i viĂªn (Admin/Staff)", description = "Há»— trá»£ phĂ¢n trang, sáº¯p xáº¿p vĂ  tĂ¬m kiáº¿m danh sĂ¡ch há»™i viĂªn.")
    public ResponseEntity<ApiResponse<PageResponse<MemberProfileResponse>>> getAllMembers(
            @Parameter(description = "Trang hiá»‡n táº¡i, báº¯t Ä‘áº§u tá»« 1", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "KĂ­ch thÆ°á»›c trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "TrÆ°á»ng dĂ¹ng Ä‘á»ƒ sáº¯p xáº¿p", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Chiá»u sáº¯p xáº¿p: ASC hoáº·c DESC", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDir,
            @Parameter(description = "Tá»« khĂ³a tĂ¬m kiáº¿m theo tĂªn/sá»‘ Ä‘iá»‡n thoáº¡i/email", example = "Nguyen")
            @RequestParam(required = false) String keyword) {

        PageResponse<MemberProfileResponse> result = memberService.getAllMembers(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.success(result, "Láº¥y danh sĂ¡ch há»™i viĂªn thĂ nh cĂ´ng"));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Táº¡o há»™i viĂªn vĂ  tĂ i khoáº£n (Admin)", description = "Admin táº¡o há»“ sÆ¡ há»™i viĂªn Ä‘á»“ng thá»i khá»Ÿi táº¡o tĂ i khoáº£n Ä‘Äƒng nháº­p.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMemberByAdmin(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMemberByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "ThĂªm há»™i viĂªn vĂ  táº¡o tĂ i khoáº£n thĂ nh cĂ´ng"));
    }

    @PatchMapping("/admin/{id}/toggle-lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "KhĂ³a/má»Ÿ khĂ³a há»™i viĂªn", description = "Chuyá»ƒn tráº¡ng thĂ¡i khĂ³a tĂ i khoáº£n cá»§a há»™i viĂªn theo ID.")
    public ResponseEntity<ApiResponse<String>> toggleMemberLock(@PathVariable Long id) {
        memberService.toggleMemberLock(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cáº­p nháº­t tráº¡ng thĂ¡i tĂ i khoáº£n thĂ nh cĂ´ng"));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Láº¥y chi tiáº¿t há»™i viĂªn", description = "Tra cá»©u thĂ´ng tin má»™t há»™i viĂªn cá»¥ thá»ƒ theo ID.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberById(@PathVariable Long id) {
        MemberProfileResponse result = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Láº¥y thĂ´ng tin há»™i viĂªn thĂ nh cĂ´ng"));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cáº­p nháº­t há»™i viĂªn", description = "Cáº­p nháº­t thĂ´ng tin há»™i viĂªn theo ID cho quáº£n trá»‹ viĂªn.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMemberByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.updateMemberByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cáº­p nháº­t thĂ´ng tin há»™i viĂªn thĂ nh cĂ´ng"));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "XĂ³a há»™i viĂªn", description = "XĂ³a há»™i viĂªn khá»i há»‡ thá»‘ng theo ID.")
    public ResponseEntity<ApiResponse<String>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "ÄĂ£ xĂ³a há»™i viĂªn khá»i há»‡ thá»‘ng"));
    }

    @GetMapping("/me")
    @Operation(summary = "Láº¥y há»“ sÆ¡ cĂ¡ nhĂ¢n", description = "Láº¥y thĂ´ng tin profile dá»±a trĂªn Token Ä‘ang Ä‘Äƒng nháº­p (Báº£o máº­t IDOR)")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(Principal principal) {
        // principal.getName() sáº½ tá»± Ä‘á»™ng mĂ³c cĂ¡i username ra tá»« JWT Token
        MemberProfileResponse result = memberService.getMyProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(result, "Láº¥y há»“ sÆ¡ cĂ¡ nhĂ¢n thĂ nh cĂ´ng"));
    }

    @PutMapping("/me")
    @Operation(summary = "Cáº­p nháº­t há»“ sÆ¡ & Chá»‰ sá»‘ BMI", description = "Cáº­p nháº­t thĂ´ng tin cĂ¡ nhĂ¢n vĂ  há»‡ thá»‘ng tá»± tĂ­nh láº¡i BMI")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMyProfile(
            @Valid @RequestBody MemberUpdateRequest request,
            Principal principal) {
        MemberProfileResponse result = memberService.updateMyProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cáº­p nháº­t há»“ sÆ¡ thĂ nh cĂ´ng"));
    }
}