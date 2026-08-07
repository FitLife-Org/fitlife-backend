package com.fitlife.ai.knowledge.controller;

import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.ai.knowledge.enums.*;
import com.fitlife.ai.knowledge.service.AiKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin/ai/knowledge")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AiKnowledgeController {
    private final AiKnowledgeService service;

    @PostMapping
    public ResponseEntity<ApiResponse<AiKnowledgeResponse>> create(@Valid @RequestBody AiKnowledgeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Success", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiKnowledgeResponse>> update(@PathVariable Long id,
            @Valid @RequestBody AiKnowledgeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Success", service.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiKnowledgeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AiKnowledgeResponse>>> search(
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) AiKnowledgeCategory category,
            @RequestParam(required=false) AiKnowledgeIndexStatus indexStatus,
            @RequestParam(required=false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Success", service.search(keyword, category, indexStatus, active, pageable)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AiKnowledgeResponse>> changeStatus(@PathVariable Long id,
            @Valid @RequestBody AiKnowledgeStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Success", service.changeStatus(id, request.active())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @PostMapping("/{id}/reindex")
    public ResponseEntity<ApiResponse<AiKnowledgeResponse>> reindex(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", service.reindex(id)));
    }

    @PostMapping("/reindex-all")
    public ResponseEntity<ApiResponse<Map<String,Integer>>> reindexAll() {
        return ResponseEntity.ok(ApiResponse.success("Success", Map.of("indexedCount", service.reindexAll())));
    }
}
