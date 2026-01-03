package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.EventBalance;
import java.util.Optional;

/**
 * Service Interface for managing
 * {@link com.mycompany.myapp.domain.EventBalance}.
 */
public interface EventBalanceService {
    /**
     * Save a eventBalance.
     *
     * @param eventBalance the entity to save.
     * @return the persisted entity.
     */
    EventBalance save(EventBalance eventBalance);

    /**
     * Updates a eventBalance.
     *
     * @param eventBalance the entity to update.
     * @return the persisted entity.
     */
    EventBalance update(EventBalance eventBalance);

    /**
     * Partially updates a eventBalance.
     *
     * @param eventBalance the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EventBalance> partialUpdate(EventBalance eventBalance);

    /**
     * Get the "id" eventBalance.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EventBalance> findOne(Long id);

    /**
     * Delete the "id" eventBalance.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Recalculate balances and settlements for an event.
     *
     * @param eventId the event id
     */
    void recalculateByEvent(Long eventId);
}
