package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Bill;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Bill}.
 */
public interface BillService {
    /**
     * Save a bill.
     *
     * @param bill the entity to save.
     * @return the persisted entity.
     */
    Bill save(Bill bill);

    /**
     * Updates a bill.
     *
     * @param bill the entity to update.
     * @return the persisted entity.
     */
    Bill update(Bill bill);

    /**
     * Partially updates a bill.
     *
     * @param bill the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Bill> partialUpdate(Bill bill);

    /**
     * Get the "id" bill.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Bill> findOne(Long id);

    /**
     * Delete the "id" bill.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
