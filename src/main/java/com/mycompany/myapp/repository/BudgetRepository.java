package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Budget;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Budget entity.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {
    @Query("select budget from Budget budget where budget.user.login = ?#{authentication.name}")
    List<Budget> findByUserIsCurrentUser();

    default Optional<Budget> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Budget> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Budget> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select budget from Budget budget left join fetch budget.user", countQuery = "select count(budget) from Budget budget")
    Page<Budget> findAllWithToOneRelationships(Pageable pageable);

    @Query("select budget from Budget budget left join fetch budget.user")
    List<Budget> findAllWithToOneRelationships();

    @Query("select budget from Budget budget left join fetch budget.user where budget.id =:id")
    Optional<Budget> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.endDate >= :date")
    List<Budget> findByUserIdAndEndDateAfter(@Param("userId") Long userId, @Param("date") Instant date);
}
