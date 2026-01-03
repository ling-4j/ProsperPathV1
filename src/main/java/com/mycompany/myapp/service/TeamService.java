package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Team;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Team}.
 */
public interface TeamService {
    /**
     * Save a team.
     *
     * @param team the entity to save.
     * @return the persisted entity.
     */
    Team save(Team team);

    /**
     * Updates a team.
     *
     * @param team the entity to update.
     * @return the persisted entity.
     */
    Team update(Team team);

    /**
     * Partially updates a team.
     *
     * @param team the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Team> partialUpdate(Team team);

    /**
     * Get the "id" team.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Team> findOne(Long id);

    /**
     * Delete the "id" team.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
