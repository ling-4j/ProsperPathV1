package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Settlement;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Settlement}.
 */
public interface SettlementService {
    /**
     * Save a settlement.
     *
     * @param settlement the entity to save.
     * @return the persisted entity.
     */
    Settlement save(Settlement settlement);

    /**
     * Updates a settlement.
     *
     * @param settlement the entity to update.
     * @return the persisted entity.
     */
    Settlement update(Settlement settlement);

    /**
     * Partially updates a settlement.
     *
     * @param settlement the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Settlement> partialUpdate(Settlement settlement);

    /**
     * Get the "id" settlement.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Settlement> findOne(Long id);

    /**
     * Delete the "id" settlement.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
