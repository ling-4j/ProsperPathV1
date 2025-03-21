package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.PeriodType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Summary entity.
 */
@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long>, JpaSpecificationExecutor<Summary> {
    @Query("select summary from Summary summary where summary.user.login = ?#{authentication.name}")
    List<Summary> findByUserIsCurrentUser();

    default Optional<Summary> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Summary> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Summary> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select summary from Summary summary left join fetch summary.user",
        countQuery = "select count(summary) from Summary summary"
    )
    Page<Summary> findAllWithToOneRelationships(Pageable pageable);

    @Query("select summary from Summary summary left join fetch summary.user")
    List<Summary> findAllWithToOneRelationships();

    @Query("select summary from Summary summary left join fetch summary.user where summary.id =:id")
    Optional<Summary> findOneWithToOneRelationships(@Param("id") Long id);

    // Thêm phương thức mới
    Optional<Summary> findByUserAndPeriodTypeAndPeriodValue(User user, PeriodType periodType, String periodValue);
}
