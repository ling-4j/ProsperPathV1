package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Budget;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Budget}.
 */
public interface BudgetService {
    /**
     * Save a budget.
     *
     * @param budget the entity to save.
     * @return the persisted entity.
     */
    Budget save(Budget budget);

    /**
     * Updates a budget.
     *
     * @param budget the entity to update.
     * @return the persisted entity.
     */
    Budget update(Budget budget);

    /**
     * Partially updates a budget.
     *
     * @param budget the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Budget> partialUpdate(Budget budget);

    /**
     * Get all the budgets with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Budget> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" budget.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Budget> findOne(Long id);

    /**
     * Delete the "id" budget.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
