package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.*;
import com.fitlife.ai.knowledge.mapper.AiKnowledgeMapper;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.*;
import com.fitlife.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl implements AiKnowledgeService {
    private final AiKnowledgeRepository repository;
    private final AiKnowledgeMapper mapper;
    private final AiKnowledgePersistenceService persistenceService;
    private final AiKnowledgeIndexService indexService;

    @Override
    public AiKnowledgeResponse create(AiKnowledgeCreateRequest request) {
        AiKnowledge k = persistenceService.createPending(request);
        if (Boolean.TRUE.equals(k.getActive())) indexService.indexKnowledge(k.getId());
        return getById(k.getId());
    }

    @Override
    public AiKnowledgeResponse update(Long id, AiKnowledgeUpdateRequest request) {
        AiKnowledge k = persistenceService.updatePending(id, request);
        if (Boolean.TRUE.equals(k.getActive())) indexService.indexKnowledge(id);
        else indexService.deleteKnowledgePoint(id);
        return getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AiKnowledgeResponse getById(Long id) {
        return mapper.toResponse(required(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiKnowledgeResponse> search(String keyword, AiKnowledgeCategory category,
            AiKnowledgeIndexStatus indexStatus, Boolean active, Pageable pageable) {
        String q = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(q, category, indexStatus, active, pageable).map(mapper::toResponse);
    }

    @Override
    public AiKnowledgeResponse changeStatus(Long id, boolean active) {
        persistenceService.changeStatus(id, active);
        if (active) indexService.indexKnowledge(id);
        else indexService.deleteKnowledgePoint(id);
        return getById(id);
    }

    @Override
    public void delete(Long id) {
        AiKnowledge k = required(id);
        if (k.getQdrantPointId()!=null) indexService.deleteKnowledgePoint(id);
        persistenceService.softDelete(id);
    }

    @Override
    public AiKnowledgeResponse reindex(Long id) {
        indexService.indexKnowledge(id);
        return getById(id);
    }

    @Override
    public int reindexAll() {
        return indexService.reindexAll();
    }

    private AiKnowledge required(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.AI_KNOWLEDGE_NOT_FOUND));
    }
}
