package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Settlement;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Settlement entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long>, JpaSpecificationExecutor<Settlement> {}
