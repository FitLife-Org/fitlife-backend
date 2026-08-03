package com.fitlife.member.controller;

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
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Member xem hồ sơ của chính mình.
     */
    @GetMapping
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Get current member profile",
            description = "Return profile of the currently authenticated member."
    )
    public ApiResponse<MemberResponse> getMyProfile() {
        MemberResponse response =
                memberService.getMyProfile();

        return ApiResponse.success(
                "Get member profile successfully",
                response
        );
    }

    /**
     * Member cập nhật các thông tin hồ sơ được phép.
     *
     * Không cập nhật:
     * - email
     * - username
     * - memberCode
     * - status
     * - joinDate
     * - avatarUrl
     */
    @PutMapping
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Update current member profile",
            description = """
                    Member can update:
                    - fullName
                    - phone
                    - gender
                    - dateOfBirth
                    - address
                    - emergencyContactName
                    - emergencyContactPhone
                    - fitnessGoal
                    - healthNote
                    """
    )
    public ApiResponse<MemberResponse> updateMyProfile(
            @Valid
            @RequestBody
            MyMemberUpdateRequest request
    ) {
        MemberResponse response =
                memberService.updateMyProfile(request);

        return ApiResponse.success(
                "Update member profile successfully",
                response
        );
    }

    /**
     * Member upload avatar bằng MultipartFile.
     */
    @PatchMapping(
            value = "/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Upload current member avatar",
            description = """
                    Upload JPG, PNG or WEBP avatar.
                    Maximum size is 5 MB.
                    Form field name must be: file
                    """
    )
    public ApiResponse<MemberResponse> updateMyAvatar(
            @RequestPart("file")
            MultipartFile file
    ) {
        MemberResponse response =
                memberService.updateMyAvatar(file);

        return ApiResponse.success(
                "Update member avatar successfully",
                response
        );
    }

    /**
     * Member xem timeline của chính mình.
     */
    @GetMapping("/timeline")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(
            summary = "Get current member timeline",
            description = """
                    Return activities of current member:
                    profile, subscription, invoice, payment,
                    check-in, body metric, AI, workout and nutrition.
                    """
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
                currentMemberService.getCurrentMember();

        PageResponse<MemberTimelineItemResponse> response =
                memberTimelineService.getTimeline(
                        currentMember.getId(),
                        pageable
                );

        return ApiResponse.success(
                "Get member timeline successfully",
                response
        );
    }
}