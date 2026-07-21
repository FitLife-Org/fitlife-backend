package com.fitlife.ai.knowledge.service;
public interface AiKnowledgeIndexService {
    void indexKnowledge(Long knowledgeId);
    void deleteKnowledgePoint(Long knowledgeId);
    int reindexAll();
}
