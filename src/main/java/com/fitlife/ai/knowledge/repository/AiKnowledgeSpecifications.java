package com.fitlife.ai.knowledge.repository;

import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AiKnowledgeSpecifications {

    private AiKnowledgeSpecifications() {
    }

    public static Specification<AiKnowledge> filter(
            String keyword,
            AiKnowledgeCategory category,
            AiKnowledgeIndexStatus indexStatus,
            Boolean active
    ) {
        return (
                root,
                query,
                criteriaBuilder
        ) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            /*
             * Không trả knowledge đã soft delete.
             */
            predicates.add(
                    criteriaBuilder.isFalse(
                            root.get(
                                    "deleted"
                            )
                    )
            );

            if (
                    keyword != null &&
                            !keyword.isBlank()
            ) {
                String searchPattern =
                        "%"
                                + keyword
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                + "%";

                Predicate codeMatches =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "code"
                                        )
                                ),
                                searchPattern
                        );

                Predicate titleMatches =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "title"
                                        )
                                ),
                                searchPattern
                        );

                Predicate contentMatches =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "content"
                                        )
                                ),
                                searchPattern
                        );

                Predicate sourceMatches =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get(
                                                "sourceName"
                                        )
                                ),
                                searchPattern
                        );

                predicates.add(
                        criteriaBuilder.or(
                                codeMatches,
                                titleMatches,
                                contentMatches,
                                sourceMatches
                        )
                );
            }

            if (category != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get(
                                        "category"
                                ),
                                category
                        )
                );
            }

            if (indexStatus != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get(
                                        "indexStatus"
                                ),
                                indexStatus
                        )
                );
            }

            if (active != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get(
                                        "active"
                                ),
                                active
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            Predicate[]::new
                    )
            );
        };
    }
}