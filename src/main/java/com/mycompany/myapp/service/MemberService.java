package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Member;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Member}.
 */
public interface MemberService {
    /**
     * Save a member.
     *
     * @param member the entity to save.
     * @return the persisted entity.
     */
    Member save(Member member);

    /**
     * Updates a member.
     *
     * @param member the entity to update.
     * @return the persisted entity.
     */
    Member update(Member member);

    /**
     * Partially updates a member.
     *
     * @param member the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Member> partialUpdate(Member member);

    /**
     * Get all the members with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Member> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" member.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Member> findOne(Long id);

    /**
     * Delete the "id" member.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
