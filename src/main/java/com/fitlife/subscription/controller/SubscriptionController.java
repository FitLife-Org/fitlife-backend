package com.fitlife.subscription.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.subscription.dto.SubscriptionCreationRequest;
import com.fitlife.subscription.dto.SubscriptionResponse;
import com.fitlife.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription Management", description = "Táº¡o vĂ  quáº£n lĂ½ Ä‘Äƒng kĂ½ gĂ³i táº­p cá»§a há»™i viĂªn")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Táº¡o Ä‘Äƒng kĂ½ gĂ³i táº­p", description = "Khá»Ÿi táº¡o subscription cho há»™i viĂªn theo gĂ³i táº­p Ä‘Ă£ chá»n vĂ  tráº¡ng thĂ¡i chá» thanh toĂ¡n.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @RequestBody SubscriptionCreationRequest request,
            Principal principal) {
        SubscriptionResponse result = subscriptionService.createSubscription(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success( "Táº¡o Ä‘Æ¡n hĂ ng PENDING thĂ nh cĂ´ng", result));
    }
}