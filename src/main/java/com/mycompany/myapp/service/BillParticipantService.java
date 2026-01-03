package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.BillParticipant;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.BillParticipant}.
 */
public interface BillParticipantService {
    /**
     * Save a billParticipant.
     *
     * @param billParticipant the entity to save.
     * @return the persisted entity.
     */
    BillParticipant save(BillParticipant billParticipant);

    /**
     * Updates a billParticipant.
     *
     * @param billParticipant the entity to update.
     * @return the persisted entity.
     */
    BillParticipant update(BillParticipant billParticipant);

    /**
     * Partially updates a billParticipant.
     *
     * @param billParticipant the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BillParticipant> partialUpdate(BillParticipant billParticipant);

    /**
     * Get the "id" billParticipant.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BillParticipant> findOne(Long id);

    /**
     * Delete the "id" billParticipant.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Save bill participants for a bill.
     *
     * @param billId    the id of the bill
     * @param memberIds the list of member ids to be added as participants
     */
    void saveBillParticipants(Long billId, List<Long> memberIds);
}
