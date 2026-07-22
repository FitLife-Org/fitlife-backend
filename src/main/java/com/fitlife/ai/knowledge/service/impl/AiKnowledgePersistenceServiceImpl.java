package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import com.fitlife.ai.knowledge.mapper.AiKnowledgeMapper;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiKnowledgePersistenceServiceImpl implements AiKnowledgePersistenceService {
    private final AiKnowledgeRepository repository;
    private final AiKnowledgeMapper mapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiKnowledge createPending(AiKnowledgeCreateRequest request) {
        String code = request.code().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw new AppException(ErrorCode.AI_KNOWLEDGE_CODE_EXISTS);
        }
        return repository.save(mapper.toEntity(request));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiKnowledge updatePending(Long id, AiKnowledgeUpdateRequest request) {
        AiKnowledge k = required(id);
        mapper.update(k, request);
        return repository.save(k);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiKnowledge changeStatus(Long id, boolean active) {
        AiKnowledge k = required(id);
        k.setActive(active);
        k.setIndexStatus(AiKnowledgeIndexStatus.PENDING);
        k.setIndexError(null);
        k.setIndexedAt(null);
        return repository.save(k);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiKnowledge markIndexed(Long id, String pointId) {
        AiKnowledge k = required(id);
        k.setQdrantPointId(pointId);
        k.setIndexStatus(AiKnowledgeIndexStatus.INDEXED);
        k.setIndexedAt(LocalDateTime.now());
        k.setIndexError(null);
        return repository.save(k);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiKnowledge markFailed(Long id, String message) {
        AiKnowledge k = required(id);
        k.setIndexStatus(AiKnowledgeIndexStatus.FAILED);
        String m = message == null || message.isBlank() ? "Unknown indexing error" : message.trim();
        k.setIndexError(m.length() <= 500 ? m : m.substring(0, 500));
        k.setIndexedAt(null);
        return repository.save(k);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiKnowledge softDelete(Long id) {
        AiKnowledge k = required(id);
        k.setDeleted(true);
        k.setActive(false);
        return repository.save(k);
    }

    private AiKnowledge required(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.AI_KNOWLEDGE_NOT_FOUND));
    }
}
