package com.fitlife.subscription.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.repository.PackageDurationRepository;
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.entity.SubscriptionHistory;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.mapper.SubscriptionMapper;
import com.fitlife.subscription.repository.SubscriptionHistoryRepository;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Mock
    private GymPackageRepository gymPackageRepository;

    @Mock
    private PackageDurationRepository packageDurationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void createSubscription_shouldThrowException_whenActiveSubscriptionExists() {
        // Mock authentication
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("member@example.com", null)
        );

        User user = new User();
        user.setId(10L);
        Member member = new Member();
        member.setId(100L);

        SubscriptionCreateRequest request = new SubscriptionCreateRequest();
        request.setPackageDurationId(1L);
        PackageDuration duration = PackageDuration.builder().id(1L).status("ACTIVE")
                .gymPackage(GymPackage.builder().id(1L).status("ACTIVE").build()).build();

        when(userRepository.findByUsername("member@example.com")).thenReturn(Optional.of(user));
        when(memberRepository.findByUserId(10L)).thenReturn(Optional.of(member));
        when(packageDurationRepository.findById(1L)).thenReturn(Optional.of(duration));
        when(subscriptionRepository.existsByMemberIdAndStatus(100L, SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThrows(AppException.class, () -> subscriptionService.createSubscription(request));
    }

    @Test
    void activateSubscriptionAfterPayment_shouldTransitionStatus() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .packageDuration(PackageDuration.builder().months(3).build())
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionService.activateSubscriptionAfterPayment(1L);

        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertNotNull(subscription.getStartDate());
        assertNotNull(subscription.getEndDate());
        verify(subscriptionHistoryRepository).save(any(SubscriptionHistory.class));
    }

    @Test
    void activateSubscriptionAfterPayment_shouldCancelOldSubscription_whenUpgrade() {
        Subscription newSub = Subscription.builder()
                .id(2L)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .note("UPGRADE_FROM_1")
                .packageDuration(PackageDuration.builder().months(6).build())
                .build();

        Subscription oldSub = Subscription.builder()
                .id(1L)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .build();

        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(newSub));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(oldSub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionService.activateSubscriptionAfterPayment(2L);

        assertEquals(SubscriptionStatus.ACTIVE, newSub.getStatus());
        assertEquals(SubscriptionStatus.CANCELLED, oldSub.getStatus());
        assertTrue(oldSub.getNote().contains("Upgraded to subscription 2"));
        verify(subscriptionHistoryRepository, times(2)).save(any(SubscriptionHistory.class));
    }

    @Test
    void activateSubscriptionAfterPayment_shouldNotOverwriteDates_whenRenewal() {
        LocalDate futureStartDate = LocalDate.now().plusDays(15);
        LocalDate futureEndDate = futureStartDate.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .id(3L)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .note("Renew of subscription 1")
                .startDate(futureStartDate)
                .endDate(futureEndDate)
                .packageDuration(PackageDuration.builder().months(1).build())
                .build();

        when(subscriptionRepository.findById(3L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionService.activateSubscriptionAfterPayment(3L);

        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(futureStartDate, subscription.getStartDate());
        assertEquals(futureEndDate, subscription.getEndDate());
        verify(subscriptionHistoryRepository).save(any(SubscriptionHistory.class));
    }
}
