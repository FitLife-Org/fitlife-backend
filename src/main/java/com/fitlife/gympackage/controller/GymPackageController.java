//package com.fitlife.gympackage.controller;
//
//import com.fitlife.common.response.ApiResponse;
//import com.fitlife.common.response.PageResponse;
//import com.fitlife.gympackage.dto.GymPackageRequest;
//import com.fitlife.gympackage.dto.GymPackageResponse;
//import com.fitlife.gympackage.service.GymPackageService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.security.SecurityRequirements;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/packages")
//@RequiredArgsConstructor
//@Tag(name = "Package Management", description = "Quáº£n lĂ½ gĂ³i táº­p, tráº¡ng thĂ¡i vĂ  danh sĂ¡ch public/admin")
//public class GymPackageController {
//
//    private final GymPackageService packageService;
//    @GetMapping
//    @PreAuthorize("permitAll()")
//    @SecurityRequirements()
//    @Operation(summary = "Danh sĂ¡ch gĂ³i táº­p cĂ´ng khai", description = "Láº¥y danh sĂ¡ch cĂ¡c gĂ³i táº­p Ä‘ang hoáº¡t Ä‘á»™ng dĂ nh cho khĂ¡ch truy cáº­p.")
//    public ResponseEntity<ApiResponse<PageResponse<GymPackageResponse>>> getActivePackages(
//            @Parameter(description = "Trang hiá»‡n táº¡i, báº¯t Ä‘áº§u tá»« 1", example = "1")
//            @RequestParam(defaultValue = "1") int page,
//            @Parameter(description = "KĂ­ch thÆ°á»›c trang", example = "10")
//            @RequestParam(defaultValue = "10") int size,
//            @Parameter(description = "TrÆ°á»ng sáº¯p xáº¿p", example = "id")
//            @RequestParam(defaultValue = "id") String sortBy,
//            @Parameter(description = "Chiá»u sáº¯p xáº¿p: ASC hoáº·c DESC", example = "DESC")
//            @RequestParam(defaultValue = "DESC") String sortDir,
//            @Parameter(description = "Tá»« khĂ³a tĂ¬m kiáº¿m gĂ³i táº­p", example = "Premium")
//            @RequestParam(required = false) String keyword
//    ) {
//        PageResponse<GymPackageResponse> result = packageService.getAllPackages(page, size, sortBy, sortDir, keyword);
//        return ResponseEntity.ok(ApiResponse.success(result, "Láº¥y danh sĂ¡ch gĂ³i táº­p thĂ nh cĂ´ng"));
//    }
//    @GetMapping("/{id}")
//    @PreAuthorize("permitAll()")
//    @SecurityRequirements()
//    @Operation(summary = "Chi tiáº¿t gĂ³i táº­p cĂ´ng khai", description = "Xem thĂ´ng tin chi tiáº¿t má»™t gĂ³i táº­p theo ID.")
//    public ResponseEntity<ApiResponse<GymPackageResponse>> getPackageById(@PathVariable Long id) {
//        GymPackageResponse result = packageService.getPackageById(id);
//        return ResponseEntity.ok(ApiResponse.success(result, "Láº¥y thĂ´ng tin chi tiáº¿t gĂ³i táº­p thĂ nh cĂ´ng"));
//    }
//
//    @GetMapping("/admin")
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
//    @Operation(summary = "Danh sĂ¡ch gĂ³i táº­p cho admin", description = "Láº¥y danh sĂ¡ch gĂ³i táº­p dĂ nh cho quáº£n trá»‹ viĂªn vá»›i phĂ¢n trang vĂ  lá»c.")
//    public ResponseEntity<ApiResponse<PageResponse<GymPackageResponse>>> getAllPackagesForAdmin(
//            @Parameter(description = "Trang hiá»‡n táº¡i, báº¯t Ä‘áº§u tá»« 1", example = "1")
//            @RequestParam(defaultValue = "1") int page,
//            @Parameter(description = "KĂ­ch thÆ°á»›c trang", example = "10")
//            @RequestParam(defaultValue = "10") int size,
//            @Parameter(description = "TrÆ°á»ng sáº¯p xáº¿p", example = "id")
//            @RequestParam(defaultValue = "id") String sortBy,
//            @Parameter(description = "Chiá»u sáº¯p xáº¿p: ASC hoáº·c DESC", example = "DESC")
//            @RequestParam(defaultValue = "DESC") String sortDir,
//            @Parameter(description = "Tá»« khĂ³a tĂ¬m kiáº¿m gĂ³i táº­p", example = "Basic")
//            @RequestParam(required = false) String keyword
//    ) {
//        PageResponse<GymPackageResponse> result = packageService.getAllPackages(page, size, sortBy, sortDir, keyword);
//        return ResponseEntity.ok(ApiResponse.success(result, "Láº¥y danh sĂ¡ch quáº£n lĂ½ gĂ³i táº­p (Admin) thĂ nh cĂ´ng"));
//    }
//
//    @PostMapping
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
//    @Operation(summary = "Táº¡o gĂ³i táº­p", description = "Táº¡o má»›i má»™t gĂ³i táº­p dĂ nh cho há»‡ thá»‘ng quáº£n trá»‹.")
//    public ResponseEntity<ApiResponse<GymPackageResponse>> createPackage(@Valid @RequestBody GymPackageRequest request) {
//        GymPackageResponse result = packageService.createPackage(request);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.created(result, "Táº¡o gĂ³i táº­p má»›i thĂ nh cĂ´ng"));
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
//    @Operation(summary = "Cáº­p nháº­t gĂ³i táº­p", description = "Cáº­p nháº­t thĂ´ng tin gĂ³i táº­p theo ID.")
//    public ResponseEntity<ApiResponse<GymPackageResponse>> updatePackage(
//            @PathVariable Long id,
//            @Valid @RequestBody GymPackageRequest request) {
//
//        GymPackageResponse result = packageService.updatePackage(id, request);
//        return ResponseEntity.ok(ApiResponse.success(result, "Cáº­p nháº­t thĂ´ng tin gĂ³i táº­p thĂ nh cĂ´ng"));
//    }
//
//    @PatchMapping("/{id}/toggle-status")
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
//    @Operation(summary = "Báº­t/táº¯t tráº¡ng thĂ¡i gĂ³i táº­p", description = "Chuyá»ƒn tráº¡ng thĂ¡i hoáº¡t Ä‘á»™ng cá»§a gĂ³i táº­p theo ID.")
//    public ResponseEntity<ApiResponse<String>> togglePackageStatus(@PathVariable Long id) {
//        packageService.togglePackageStatus(id);
//        return ResponseEntity.ok(ApiResponse.success(null, "Cáº­p nháº­t tráº¡ng thĂ¡i gĂ³i táº­p thĂ nh cĂ´ng"));
//    }
//}