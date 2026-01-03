package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Bill;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Bill entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    @Query(
        """
            select b
            from Bill b
            join fetch b.payer
            where b.event.id = :eventId
        """
    )
    List<Bill> findAllByEventId(@Param("eventId") Long eventId);
}
