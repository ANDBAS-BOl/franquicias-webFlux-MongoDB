package com.seti.franchises.infrastructure.persistence.repository;

import com.seti.franchises.infrastructure.persistence.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for FranchiseDocument.
 * Infrastructure detail: only knows about documents, not domain entities.
 */
@Repository
public interface FranchiseMongoRepository extends ReactiveMongoRepository<FranchiseDocument, String> {
}
