package com.fitlife.gympackage.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.gympackage.dto.PackageDurationCreateRequest;
import com.fitlife.gympackage.dto.PackageDurationResponse;
import com.fitlife.gympackage.dto.PackageDurationUpdateRequest;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.gympackage.mapper.PackageDurationMapper;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.repository.PackageDurationRepository;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageDurationServiceImplTest {

    @Mock
    private PackageDurationRepository packageDurationRepository;

    @Mock
    private GymPackageRepository gymPackageRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PackageDurationMapper packageDurationMapper;

    @InjectMocks
    private PackageDurationServiceImpl packageDurationService;

    @Test
    void getActiveDurationsList_shouldReturnActiveDurations() {
        PackageDuration duration = PackageDuration.builder().id(1L).code("DUR_1M").status("ACTIVE").build();
        when(packageDurationRepository.findByStatus("ACTIVE")).thenReturn(List.of(duration));
        when(packageDurationMapper.toResponse(any(PackageDuration.class)))
                .thenReturn(PackageDurationResponse.builder().id(1L).code("DUR_1M").status("ACTIVE").build());

        List<PackageDurationResponse> result = packageDurationService.getActiveDurationsList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DUR_1M", result.get(0).getCode());
    }

    @Test
    void createDuration_shouldThrowException_whenCodeExists() {
        PackageDurationCreateRequest request = PackageDurationCreateRequest.builder().code("DUR_1M").build();
        when(packageDurationRepository.existsByCode("DUR_1M")).thenReturn(true);

        assertThrows(AppException.class, () -> packageDurationService.createDuration(request));
    }

    @Test
    void createDuration_shouldThrowException_whenPackageNotFound() {
        PackageDurationCreateRequest request = PackageDurationCreateRequest.builder().code("DUR_1M").gymPackageId(999L).build();
        when(packageDurationRepository.existsByCode("DUR_1M")).thenReturn(false);
        when(packageDurationMapper.toEntity(request)).thenReturn(new PackageDuration());
        when(gymPackageRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> packageDurationService.createDuration(request));
    }

    @Test
    void createDuration_shouldSave_whenValid() {
        PackageDurationCreateRequest request = PackageDurationCreateRequest.builder()
                .code("DUR_1M")
                .name("1 Month")
                .months(1)
                .price(BigDecimal.valueOf(100))
                .discountPrice(BigDecimal.valueOf(80))
                .gymPackageId(1L)
                .status("ACTIVE")
                .build();

        GymPackage gymPackage = GymPackage.builder().id(1L).name("VIP").build();
        PackageDuration duration = new PackageDuration();

        when(packageDurationRepository.existsByCode("DUR_1M")).thenReturn(false);
        when(packageDurationMapper.toEntity(request)).thenReturn(duration);
        when(gymPackageRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(gymPackage));
        when(packageDurationRepository.findByGymPackageIdAndMonthsAndStatus(1L, 1, "ACTIVE")).thenReturn(Optional.empty());
        when(packageDurationRepository.save(any(PackageDuration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(packageDurationMapper.toResponse(any(PackageDuration.class)))
                .thenReturn(PackageDurationResponse.builder().id(10L).code("DUR_1M").status("ACTIVE").build());

        PackageDurationResponse response = packageDurationService.createDuration(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    void updateDuration_shouldThrowException_whenNotFound() {
        when(packageDurationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> packageDurationService.updateDuration(1L, new PackageDurationUpdateRequest()));
    }

    @Test
    void deleteDuration_shouldThrowException_whenHasActiveSubscriptions() {
        PackageDuration duration = PackageDuration.builder().id(1L).build();
        when(packageDurationRepository.findById(1L)).thenReturn(Optional.of(duration));
        when(subscriptionRepository.existsByPackageDurationIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThrows(AppException.class, () -> packageDurationService.deleteDuration(1L));
    }

    @Test
    void deleteDuration_shouldDelete_whenNoActiveSubscriptions() {
        PackageDuration duration = PackageDuration.builder().id(1L).build();
        when(packageDurationRepository.findById(1L)).thenReturn(Optional.of(duration));
        when(subscriptionRepository.existsByPackageDurationIdAndStatus(1L, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(subscriptionRepository.existsByPackageDurationIdAndStatus(1L, SubscriptionStatus.PENDING_PAYMENT)).thenReturn(false);
        when(subscriptionRepository.existsByPackageDurationIdAndStatus(1L, SubscriptionStatus.PAUSED)).thenReturn(false);
        when(subscriptionRepository.existsByPackageDurationIdAndStatus(1L, SubscriptionStatus.SUSPENDED)).thenReturn(false);

        packageDurationService.deleteDuration(1L);

        verify(packageDurationRepository).delete(duration);
    }
}
