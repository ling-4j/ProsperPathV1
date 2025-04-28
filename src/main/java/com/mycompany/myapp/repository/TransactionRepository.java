package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Transaction entity.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @Query("select transaction from Transaction transaction where transaction.user.login = ?#{authentication.name}")
    List<Transaction> findByUserIsCurrentUser();

    default Optional<Transaction> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Transaction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Transaction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select transaction from Transaction transaction left join fetch transaction.user",
        countQuery = "select count(transaction) from Transaction transaction"
    )
    Page<Transaction> findAllWithToOneRelationships(Pageable pageable);

    @Query("select transaction from Transaction transaction left join fetch transaction.user")
    List<Transaction> findAllWithToOneRelationships();

    @Query("select transaction from Transaction transaction left join fetch transaction.user where transaction.id =:id")
    Optional<Transaction> findOneWithToOneRelationships(@Param("id") Long id);

    // Thêm phương thức để tìm giao dịch theo userId và khoảng thời gian
    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, Instant startDate, Instant endDate);

    /**
     * Get all transactions by userId.
     *
     * @param userId the id of the user.
     * @return the list of transactions.
     */
    List<Transaction> findByUserId(Long userId);
}
