package com.seti.franchises.infrastructure.persistence.adapter;

import com.seti.franchises.domain.entity.Franchise;
import com.seti.franchises.domain.port.FranchiseRepository;
import com.seti.franchises.infrastructure.persistence.document.FranchiseDocument;
import com.seti.franchises.infrastructure.persistence.mapper.FranchisePersistenceMapper;
import com.seti.franchises.infrastructure.persistence.repository.FranchiseMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter that implements the domain port FranchiseRepository using ReactiveMongoRepository.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private final FranchiseMongoRepository mongoRepository;
    private final FranchisePersistenceMapper mapper;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return Mono.justOrEmpty(franchise)
                .map(mapper::toDocument)
                .flatMap(mongoRepository::save)
                .map(mapper::toEntity)
                .doOnNext(saved -> log.debug("Franchise saved: id={}", saved != null ? saved.getId() : null))
                .doOnError(e -> log.error("Error saving franchise", e));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return Mono.justOrEmpty(id)
                .flatMap(mongoRepository::findById)
                .map(mapper::toEntity)
                .doOnNext(f -> log.debug("Franchise found: id={}", id))
                .doOnError(e -> log.error("Error finding franchise by id={}", id, e));
    }

    @Override
    public Flux<Franchise> findAll() {
        return mongoRepository.findAll()
                .map(mapper::toEntity)
                .doOnComplete(() -> log.debug("Find all franchises completed"))
                .doOnError(e -> log.error("Error finding all franchises", e));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.justOrEmpty(id)
                .flatMap(mongoRepository::deleteById)
                .then()
                .doOnSuccess(v -> log.debug("Franchise deleted: id={}", id))
                .doOnError(e -> log.error("Error deleting franchise id={}", id, e));
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        return Mono.justOrEmpty(id)
                .flatMap(mongoRepository::existsById)
                .defaultIfEmpty(false)
                .doOnError(e -> log.error("Error checking existence for franchise id={}", id, e));
    }
}
