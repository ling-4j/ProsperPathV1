package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.service.criteria.TransactionCriteria;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Transaction} entities in the database.
 * The main input is a {@link TransactionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Transaction} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TransactionQueryService extends QueryService<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionQueryService.class);

    private final TransactionRepository transactionRepository;

    public TransactionQueryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Return a {@link Page} of {@link Transaction} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> findByCriteria(TransactionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TransactionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.count(specification);
    }

    /**
     * Function to convert {@link TransactionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Transaction> createSpecification(TransactionCriteria criteria) {
        Specification<Transaction> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Transaction_.id));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Transaction_.amount));
            }
            if (criteria.getTransactionType() != null) {
                specification = specification.and(buildSpecification(criteria.getTransactionType(), Transaction_.transactionType));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), Transaction_.description));
            }
            if (criteria.getTransactionDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTransactionDate(), Transaction_.transactionDate));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Transaction_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Transaction_.updatedAt));
            }
            if (criteria.getCategoryId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getCategoryId(), root -> root.join(Transaction_.category, JoinType.LEFT).get(Category_.id))
                );
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getUserId(), root -> root.join(Transaction_.user, JoinType.LEFT).get(User_.id))
                );
            }
        }
        return specification;
    }
}
