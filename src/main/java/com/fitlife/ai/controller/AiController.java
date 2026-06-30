//package com.fitlife.ai.controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fitlife.ai.service.AiService;
//import com.fitlife.ai.dto.AiWorkoutRequest;
//import com.fitlife.ai.entity.AiWorkoutPlan;
//import com.fitlife.common.dto.response.ApiResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//
//@RestController
//@RequestMapping("ai")
//@RequiredArgsConstructor
//@Tag(name = "AI Workout", description = "TĂ¡ÂºÂ¡o vÄ‚Â  quĂ¡ÂºÂ£n lÄ‚Â½ lĂ¡Â»â€¹ch tĂ¡ÂºÂ­p thÄ‚Â´ng minh bĂ¡ÂºÂ±ng AI")
//public class AiController {
//
//    private final AiService aiService;
//
//    @PostMapping("/workout-plan")
//    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
//    @Operation(summary = "TĂ¡ÂºÂ¡o lĂ¡Â»â€¹ch tĂ¡ÂºÂ­p bĂ¡ÂºÂ±ng AI", description = "Sinh kĂ¡ÂºÂ¿ hoĂ¡ÂºÂ¡ch tĂ¡ÂºÂ­p luyĂ¡Â»â€¡n cÄ‚Â¡ nhÄ‚Â¢n hÄ‚Â³a dĂ¡Â»Â±a trÄ‚Âªn mĂ¡Â»Â¥c tiÄ‚Âªu, trÄ‚Â¬nh Ă„â€˜Ă¡Â»â„¢ vÄ‚Â  sĂ¡Â»â€˜ buĂ¡Â»â€¢i tĂ¡ÂºÂ­p mĂ¡Â»â€”i tuĂ¡ÂºÂ§n.")
//    public ResponseEntity<ApiResponse<JsonNode>> generatePlan(
//            @Valid @RequestBody AiWorkoutRequest request,
//            Principal principal) {
//
//        JsonNode aiPlan = aiService.generateWorkoutPlan(principal.getName(), request);
//
//        return ResponseEntity.ok(ApiResponse.success(aiPlan, "PhÄ‚Â¡c Ă„â€˜Ă¡Â»â€œ cÄ‚Â¡ nhÄ‚Â¢n hÄ‚Â³a Ă„â€˜Ä‚Â£ Ă„â€˜Ă†Â°Ă¡Â»Â£c AI tĂ¡ÂºÂ¡o vÄ‚Â  lĂ†Â°u vÄ‚Â o lĂ¡Â»â€¹ch sĂ¡Â»Â­ thÄ‚Â nh cÄ‚Â´ng!"));
//    }
//
//    @GetMapping("/history")
//    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
//    @Operation(summary = "LĂ¡Â»â€¹ch sĂ¡Â»Â­ AI workout", description = "LĂ¡ÂºÂ¥y danh sÄ‚Â¡ch cÄ‚Â¡c kĂ¡ÂºÂ¿ hoĂ¡ÂºÂ¡ch tĂ¡ÂºÂ­p luyĂ¡Â»â€¡n do AI Ă„â€˜Ä‚Â£ tĂ¡ÂºÂ¡o cho hĂ¡Â»â„¢i viÄ‚Âªn.")
//    public ResponseEntity<ApiResponse<List<AiWorkoutPlan>>> getHistory(Principal principal) {
//
//        List<AiWorkoutPlan> history = aiService.getMemberHistory(principal.getName());
//
//        return ResponseEntity.ok(ApiResponse.success(history, "LĂ¡ÂºÂ¥y danh sÄ‚Â¡ch lĂ¡Â»â€¹ch sĂ¡Â»Â­ tĂ†Â° vĂ¡ÂºÂ¥n AI thÄ‚Â nh cÄ‚Â´ng."));
//    }
//}
