package com.fitlife.ai.knowledge.controller;

import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
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
    public ResponseEntity<AiKnowledgeResponse> create(@Valid @RequestBody AiKnowledgeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public AiKnowledgeResponse update(@PathVariable Long id,
            @Valid @RequestBody AiKnowledgeUpdateRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    public AiKnowledgeResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public Page<AiKnowledgeResponse> search(
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) AiKnowledgeCategory category,
            @RequestParam(required=false) AiKnowledgeIndexStatus indexStatus,
            @RequestParam(required=false) Boolean active,
            Pageable pageable) {
        return service.search(keyword, category, indexStatus, active, pageable);
    }

    @PatchMapping("/{id}/status")
    public AiKnowledgeResponse changeStatus(@PathVariable Long id,
            @Valid @RequestBody AiKnowledgeStatusRequest request) {
        return service.changeStatus(id, request.active());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/reindex")
    public AiKnowledgeResponse reindex(@PathVariable Long id) {
        return service.reindex(id);
    }

    @PostMapping("/reindex-all")
    public Map<String,Integer> reindexAll() {
        return Map.of("indexedCount", service.reindexAll());
    }
}
