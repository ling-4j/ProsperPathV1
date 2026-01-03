package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.BillParticipant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BillParticipant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BillParticipantRepository extends JpaRepository<BillParticipant, Long>, JpaSpecificationExecutor<BillParticipant> {
    List<BillParticipant> findAllByBillId(Long billId);

    @Modifying
    @Query("delete from BillParticipant bp where bp.bill.id = :billId")
    void deleteByBillId(@Param("billId") Long billId);
}
