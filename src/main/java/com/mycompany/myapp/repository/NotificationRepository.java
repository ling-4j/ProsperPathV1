package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Notification;
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
 * Spring Data JPA repository for the Notification entity.
 */
@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query("select notification from Notification notification where notification.user.login = ?#{authentication.name}")
    List<Notification> findByUserIsCurrentUser();

    default Optional<Notification> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Notification> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Notification> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select notification from Notification notification left join fetch notification.user", countQuery = "select count(notification) from Notification notification")
    Page<Notification> findAllWithToOneRelationships(Pageable pageable);

    @Query("select notification from Notification notification left join fetch notification.user")
    List<Notification> findAllWithToOneRelationships();

    @Query("select notification from Notification notification left join fetch notification.user where notification.id =:id")
    Optional<Notification> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("""
                SELECT b
                FROM Budget b
                WHERE b.category.id = :categoryId
                  AND b.user.id = :userId
                  AND b.status = 'ACTIVE'
                  AND :transactionDate BETWEEN b.startDate AND b.endDate
            """)
    List<Budget> findMatchingBudgetsWithTransactionDate(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId,
            @Param("transactionDate") Instant transactionDate);

}
