package com.seti.franchises.domain.port;

import com.seti.franchises.domain.entity.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Port (repository interface) for franchise persistence.
 * Dependency inversion: the domain defines the contract; infrastructure implements it.
 */
public interface FranchiseRepository {

    /**
     * Saves or updates a franchise.
     *
     * @param franchise the franchise to save
     * @return Mono emitting the saved franchise (with id if new)
     */
    Mono<Franchise> save(Franchise franchise);

    /**
     * Finds a franchise by its id.
     *
     * @param id franchise id
     * @return Mono emitting the franchise or empty if not found
     */
    Mono<Franchise> findById(String id);

    /**
     * Returns all franchises.
     *
     * @return Flux of all franchises
     */
    Flux<Franchise> findAll();

    /**
     * Deletes a franchise by id.
     *
     * @param id franchise id
     * @return Mono that completes when deleted or empty if not found
     */
    Mono<Void> deleteById(String id);

    /**
     * Checks if a franchise exists by id.
     *
     * @param id franchise id
     * @return Mono emitting true if exists, false otherwise
     */
    Mono<Boolean> existsById(String id);
}
