package com.fitlife.member.controller;

import com.fitlife.checkin.dto.MemberQrResponse;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.request.MyMemberUpdateRequest;
import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.member.service.MemberService;
import com.fitlife.member.timeline.dto.MemberTimelineItemResponse;
import com.fitlife.member.timeline.service.MemberTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/members/me")
@RequiredArgsConstructor
@Tag(
        name = "Member Profile",
        description = "APIs for current member profile, avatar and timeline"
)
@SecurityRequirement(name = "bearerAuth")
public class MemberController {

    private final MemberService memberService;

    private final CurrentMemberService currentMemberService;

    private final MemberTimelineService memberTimelineService;

    private final CheckInService checkInService;

    @GetMapping("/qr")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Get current member QR code data"
    )
    public ApiResponse<MemberQrResponse> getMyQr(Authentication authentication) {
        return ApiResponse.success(
                "Get member QR successfully",
                checkInService.getMemberQr(authentication.getName())
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Get current member profile"
    )
    public ApiResponse<MemberResponse> getMyProfile() {
        return ApiResponse.success(
                "Get member profile successfully",
                memberService.getMyProfile()
        );
    }

    @PutMapping
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Update current member profile"
    )
    public ApiResponse<MemberResponse> updateMyProfile(
            @Valid
            @RequestBody
            MyMemberUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update member profile successfully",
                memberService.updateMyProfile(
                        request
                )
        );
    }

    @PatchMapping(
            value = "/avatar",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Upload current member avatar",
            description = """
                    Supported:
                    - JPG
                    - PNG
                    - WEBP
                    
                    Maximum size: 5 MB.
                    Multipart field: file.
                    """
    )
    public ApiResponse<MemberResponse> updateMyAvatar(
            @RequestPart("file")
            MultipartFile file
    ) {
        return ApiResponse.success(
                "Update member avatar successfully",
                memberService.updateMyAvatar(
                        file
                )
        );
    }

    @GetMapping("/timeline")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Get current member timeline"
    )
    public ApiResponse<
            PageResponse<MemberTimelineItemResponse>
            > getMyTimeline(
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "occurredAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        return ApiResponse.success(
                "Get member timeline successfully",
                memberTimelineService.getTimeline(
                        currentMember.getId(),
                        pageable
                )
        );
    }
}