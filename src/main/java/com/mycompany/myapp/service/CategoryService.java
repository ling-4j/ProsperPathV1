package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Category;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Category}.
 */
public interface CategoryService {
    /**
     * Save a category.
     *
     * @param category the entity to save.
     * @return the persisted entity.
     */
    Category save(Category category);

    /**
     * Updates a category.
     *
     * @param category the entity to update.
     * @return the persisted entity.
     */
    Category update(Category category);

    /**
     * Partially updates a category.
     *
     * @param category the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Category> partialUpdate(Category category);

    /**
     * Get all the categories with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Category> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" category.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Category> findOne(Long id);

    /**
     * Delete the "id" category.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
