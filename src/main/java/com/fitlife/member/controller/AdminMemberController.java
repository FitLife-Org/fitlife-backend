package com.fitlife.member.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.request.AdminMemberStatusUpdateRequest;
import com.fitlife.member.dto.request.MemberCreateRequest;
import com.fitlife.member.dto.request.MemberUpdateRequest;
import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.dto.response.MemberSummaryResponse;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.service.MemberService;
import com.fitlife.member.timeline.dto.MemberTimelineItemResponse;
import com.fitlife.member.timeline.service.MemberTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
@Tag(
        name = "Admin - Member Management",
        description = "APIs for Admin and Staff to manage members"
)
@SecurityRequirement(name = "bearerAuth")
public class AdminMemberController {

    private final MemberService memberService;

    private final MemberTimelineService memberTimelineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(
            summary = "Get member list"
    )
    public ApiResponse<
            PageResponse<MemberSummaryResponse>
            > getAllMembers(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    required = false
            )
            String keyword,

            @RequestParam(
                    required = false
            )
            MemberStatus status
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        PageResponse<MemberSummaryResponse> response =
                memberService.getAllMembersForAdmin(
                        keyword,
                        status,
                        pageable
                );

        return ApiResponse.success(
                "Get member list successfully",
                response
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create member"
    )
    public ApiResponse<MemberResponse> createMemberByAdmin(
            @Valid
            @RequestBody
            MemberCreateRequest request
    ) {
        MemberResponse response =
                memberService.createMemberByAdmin(
                        request
                );

        return ApiResponse.created(
                "Create member successfully",
                response
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(
            summary = "Get member detail"
    )
    public ApiResponse<MemberResponse> getMemberDetail(
            @PathVariable
            Long id
    ) {
        MemberResponse response =
                memberService.getMemberDetailForAdmin(
                        id
                );

        return ApiResponse.success(
                "Get member detail successfully",
                response
        );
    }

    @GetMapping("/code/{memberCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(
            summary = "Get member by member code"
    )
    public ApiResponse<MemberResponse> getMemberByCode(
            @PathVariable
            String memberCode
    ) {
        MemberResponse response =
                memberService.getMemberByCodeForAdmin(
                        memberCode
                );

        return ApiResponse.success(
                "Get member by code successfully",
                response
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update member"
    )
    public ApiResponse<MemberResponse> updateMemberByAdmin(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            MemberUpdateRequest request
    ) {
        MemberResponse response =
                memberService.updateMemberByAdmin(
                        id,
                        request
                );

        return ApiResponse.success(
                "Update member successfully",
                response
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update member status"
    )
    public ApiResponse<MemberResponse> updateMemberStatus(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            AdminMemberStatusUpdateRequest request
    ) {
        MemberResponse response =
                memberService.updateMemberStatusByAdmin(
                        id,
                        request
                );

        return ApiResponse.success(
                "Update member status successfully",
                response
        );
    }

    /**
     * Soft delete.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Soft delete member"
    )
    public ApiResponse<Void> deleteMemberByAdmin(
            @PathVariable
            Long id
    ) {
        memberService.deleteMemberByAdmin(
                id
        );

        return ApiResponse.success(
                "Delete member successfully"
        );
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Restore member"
    )
    public ApiResponse<Void> restoreMemberByAdmin(
            @PathVariable
            Long id
    ) {
        memberService.restoreMemberByAdmin(
                id
        );

        return ApiResponse.success(
                "Restore member successfully"
        );
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(
            summary = "Get member timeline"
    )
    public ApiResponse<
            PageResponse<MemberTimelineItemResponse>
            > getMemberTimeline(
            @PathVariable
            Long id,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "occurredAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<MemberTimelineItemResponse> response =
                memberTimelineService.getTimeline(
                        id,
                        pageable
                );

        return ApiResponse.success(
                "Get member timeline successfully",
                response
        );
    }
}