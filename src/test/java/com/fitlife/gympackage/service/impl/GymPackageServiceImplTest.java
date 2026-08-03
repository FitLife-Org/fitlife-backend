package com.fitlife.gympackage.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.response.PageResponse;
import com.fitlife.gympackage.dto.GymPackageCreateRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.dto.GymPackageUpdateRequest;
import com.fitlife.gympackage.dto.GymPackageVisibilityRequest;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.mapper.GymPackageMapper;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymPackageServiceImplTest {

    @Mock
    private GymPackageRepository gymPackageRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private GymPackageMapper gymPackageMapper;

    @InjectMocks
    private GymPackageServiceImpl gymPackageService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPackagesList_shouldFilterActiveOnly_forGuestOrMember() {
        SecurityContextHolder.clearContext(); // Guest

        Pageable pageable = PageRequest.of(0, 10);
        GymPackage pkg = GymPackage.builder().id(1L).name("Basic").status("ACTIVE").isDeleted(false).build();
        Page<GymPackage> page = new PageImpl<>(List.of(pkg));

        when(gymPackageRepository.searchPackages(null, null, "ACTIVE", pageable))
                .thenReturn(page);
        when(gymPackageMapper.toResponse(any(GymPackage.class)))
                .thenReturn(GymPackageResponse.builder().id(1L).name("Basic").status("ACTIVE").build());

        PageResponse<GymPackageResponse> response = gymPackageService.getPackagesList(null, null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Basic", response.getContent().get(0).getName());
        verify(gymPackageRepository).searchPackages(null, null, "ACTIVE", pageable);
    }

    @Test
    void getPackagesList_shouldAllowAllStatus_forAdminOrStaff() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Pageable pageable = PageRequest.of(0, 10);
        GymPackage pkg = GymPackage.builder().id(1L).name("Basic").status("INACTIVE").isDeleted(false).build();
        Page<GymPackage> page = new PageImpl<>(List.of(pkg));

        when(gymPackageRepository.searchPackages(null, null, "INACTIVE", pageable))
                .thenReturn(page);
        when(gymPackageMapper.toResponse(any(GymPackage.class)))
                .thenReturn(GymPackageResponse.builder().id(1L).name("Basic").status("INACTIVE").build());

        PageResponse<GymPackageResponse> response = gymPackageService.getPackagesList(null, null, "INACTIVE", pageable);

        assertNotNull(response);
        assertEquals("INACTIVE", response.getContent().get(0).getStatus());
        verify(gymPackageRepository).searchPackages(null, null, "INACTIVE", pageable);
    }

    @Test
    void getPackageById_shouldThrowException_whenPackageIsInactiveAndUserIsGuest() {
        SecurityContextHolder.clearContext();

        GymPackage pkg = GymPackage.builder().id(1L).name("Premium").status("INACTIVE").isDeleted(false).build();
        when(gymPackageRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(pkg));

        assertThrows(AppException.class, () -> gymPackageService.getPackageById(1L));
    }

    @Test
    void getPackageById_shouldReturnPackage_whenPackageIsInactiveAndUserIsAdmin() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        GymPackage pkg = GymPackage.builder().id(1L).name("Premium").status("INACTIVE").isDeleted(false).build();
        when(gymPackageRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(pkg));
        when(gymPackageMapper.toResponse(pkg))
                .thenReturn(GymPackageResponse.builder().id(1L).name("Premium").status("INACTIVE").build());

        GymPackageResponse response = gymPackageService.getPackageById(1L);

        assertNotNull(response);
        assertEquals("Premium", response.getName());
    }

    @Test
    void createPackage_shouldThrowException_whenNameExists() {
        GymPackageCreateRequest request = GymPackageCreateRequest.builder().name("Exist").code("EXIST-01").build();
        when(gymPackageRepository.existsByNameAndIsDeletedFalse("Exist")).thenReturn(true);

        assertThrows(AppException.class, () -> gymPackageService.createPackage(request));
    }

    @Test
    void createPackage_shouldSave_whenDataIsValid() {
        GymPackageCreateRequest request = GymPackageCreateRequest.builder()
                .name("New Pkg")
                .code("NEW-01")
                .basePrice(BigDecimal.TEN)
                .packageType("VIP")
                .hasAiWorkoutPlan(true)
                .hasNutritionPlan(true)
                .ptSessionsPerMonth(4)
                .status("ACTIVE")
                .build();

        when(gymPackageRepository.existsByNameAndIsDeletedFalse("New Pkg")).thenReturn(false);
        when(gymPackageRepository.existsByCodeAndIsDeletedFalse("NEW-01")).thenReturn(false);
        when(gymPackageRepository.save(any(GymPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gymPackageMapper.toResponse(any(GymPackage.class))).thenAnswer(invocation -> {
            GymPackage pkg = invocation.getArgument(0);
            return GymPackageResponse.builder()
                    .id(1L)
                    .name(pkg.getName())
                    .code(pkg.getCode())
                    .status(pkg.getStatus())
                    .build();
        });

        GymPackageResponse response = gymPackageService.createPackage(request);

        assertNotNull(response);
        assertEquals("New Pkg", response.getName());
        assertEquals("NEW-01", response.getCode());
    }

    @Test
    void updateVisibility_shouldThrowException_whenStatusIsBlank() {
        GymPackage pkg = GymPackage.builder().id(1L).name("Pkg").status("ACTIVE").build();
        when(gymPackageRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(pkg));

        GymPackageVisibilityRequest request = GymPackageVisibilityRequest.builder().status("").build();

        assertThrows(AppException.class, () -> gymPackageService.updateVisibility(1L, request));
    }

    @Test
    void deletePackage_shouldThrowException_whenHasActiveSubscriptions() {
        GymPackage pkg = GymPackage.builder().id(1L).name("Pkg").status("ACTIVE").build();
        when(gymPackageRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(pkg));
        when(subscriptionRepository.existsByGymPackageIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThrows(AppException.class, () -> gymPackageService.deletePackage(1L));
    }

    @Test
    void deletePackage_shouldSoftDelete_whenNoActiveSubscriptions() {
        GymPackage pkg = GymPackage.builder().id(1L).name("Pkg").status("ACTIVE").isDeleted(false).build();
        when(gymPackageRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(pkg));
        when(subscriptionRepository.existsByGymPackageIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(subscriptionRepository.existsByGymPackageIdAndStatus(1L, SubscriptionStatus.PENDING_PAYMENT)).thenReturn(false);
        when(subscriptionRepository.existsByGymPackageIdAndStatus(1L, SubscriptionStatus.PAUSED)).thenReturn(false);
        when(subscriptionRepository.existsByGymPackageIdAndStatus(1L, SubscriptionStatus.SUSPENDED)).thenReturn(false);

        gymPackageService.deletePackage(1L);

        assertTrue(pkg.getIsDeleted());
        verify(gymPackageRepository).save(pkg);
    }
}
