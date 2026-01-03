package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.EventBalance;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventBalance entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventBalanceRepository extends JpaRepository<EventBalance, Long>, JpaSpecificationExecutor<EventBalance> {
    @Modifying
    @Query("delete from EventBalance eb where eb.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}
