package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.TeamMember;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.TeamMember}.
 */
public interface TeamMemberService {
    /**
     * Save a teamMember.
     *
     * @param teamMember the entity to save.
     * @return the persisted entity.
     */
    TeamMember save(TeamMember teamMember);

    /**
     * Updates a teamMember.
     *
     * @param teamMember the entity to update.
     * @return the persisted entity.
     */
    TeamMember update(TeamMember teamMember);

    /**
     * Partially updates a teamMember.
     *
     * @param teamMember the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TeamMember> partialUpdate(TeamMember teamMember);

    /**
     * Get the "id" teamMember.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TeamMember> findOne(Long id);

    /**
     * Delete the "id" teamMember.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
