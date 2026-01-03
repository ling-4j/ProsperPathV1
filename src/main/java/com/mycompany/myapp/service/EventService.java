package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Event;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Event}.
 */
public interface EventService {
    /**
     * Save a event.
     *
     * @param event the entity to save.
     * @return the persisted entity.
     */
    Event save(Event event);

    /**
     * Updates a event.
     *
     * @param event the entity to update.
     * @return the persisted entity.
     */
    Event update(Event event);

    /**
     * Partially updates a event.
     *
     * @param event the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Event> partialUpdate(Event event);

    /**
     * Get the "id" event.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Event> findOne(Long id);

    /**
     * Delete the "id" event.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
