package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Notification;
import com.mycompany.myapp.domain.Transaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing
 * {@link com.mycompany.myapp.domain.Notification}.
 */
public interface NotificationService {
    /**
     * Save a notification.
     *
     * @param notification the entity to save.
     * @return the persisted entity.
     */
    Notification save(Notification notification);

    /**
     * Updates a notification.
     *
     * @param notification the entity to update.
     * @return the persisted entity.
     */
    Notification update(Notification notification);

    /**
     * Partially updates a notification.
     *
     * @param notification the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Notification> partialUpdate(Notification notification);

    /**
     * Get all the notifications with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Notification> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" notification.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Notification> findOne(Long id);

    /**
     * Delete the "id" notification.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Creates a notification for a transaction if it matches active budgets and
     * exceeds/completes the budget.
     *
     * @param userId      The ID of the user.
     * @param transaction The transaction to check.
     */
    void createNotificationForTransaction(Long userId, Transaction transaction);

    /**
     * Creates a warning notification for a transaction if it reaches or exceeds 85%
     * of the budget.
     *
     * @param userId      The ID of the user.
     * @param transaction The transaction to check.
     */
    void createWarningNotificationForTransaction(Long userId, Transaction transaction);
}
