package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Transaction}.
 */
public interface TransactionService {
    /**
     * Save a transaction.
     *
     * @param transaction the entity to save.
     * @return the persisted entity.
     */
    Transaction save(Transaction transaction);

    /**
     * Updates a transaction.
     *
     * @param transaction the entity to update.
     * @return the persisted entity.
     */
    Transaction update(Transaction transaction);

    /**
     * Partially updates a transaction.
     *
     * @param transaction the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Transaction> partialUpdate(Transaction transaction);

    /**
     * Get all the transactions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Transaction> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" transaction.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Transaction> findOne(Long id);

    /**
     * Delete the "id" transaction.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Get all transactions by userId.
     *
     * @param userId the id of the user.
     * @return the list of transactions.
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Import transactions from a file.
     *
     * @param file the file containing transactions to import.
     */
    void importFromFile(MultipartFile file);
}
