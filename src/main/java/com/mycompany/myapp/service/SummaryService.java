package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.domain.Transaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Summary}.
 */
public interface SummaryService {
    /**
     * Save a summary.
     *
     * @param summary the entity to save.
     * @return the persisted entity.
     */
    Summary save(Summary summary);

    /**
     * Updates a summary.
     *
     * @param summary the entity to update.
     * @return the persisted entity.
     */
    Summary update(Summary summary);

    /**
     * Partially updates a summary.
     *
     * @param summary the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Summary> partialUpdate(Summary summary);

    /**
     * Get all the summaries with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Summary> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" summary.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Summary> findOne(Long id);

    /**
     * Delete the "id" summary.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Update or create a summary based on a transaction.
     *
     * @param userId the ID of the user.
     * @param oldTransaction the old transaction (before update/delete, null if new).
     * @param newTransaction the new transaction (after create/update, null if delete).
     */
    void updateSummaryForTransaction(Long userId, Transaction oldTransaction, Transaction newTransaction);
}
