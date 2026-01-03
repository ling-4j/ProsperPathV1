package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Budget;
import com.mycompany.myapp.repository.BudgetRepository;
import com.mycompany.myapp.service.criteria.BudgetCriteria;
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
 * Service for executing complex queries for {@link Budget} entities in the database.
 * The main input is a {@link BudgetCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Budget} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BudgetQueryService extends QueryService<Budget> {

    private static final Logger LOG = LoggerFactory.getLogger(BudgetQueryService.class);

    private final BudgetRepository budgetRepository;

    public BudgetQueryService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * Return a {@link Page} of {@link Budget} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Budget> findByCriteria(BudgetCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Budget> specification = createSpecification(criteria);
        return budgetRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BudgetCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Budget> specification = createSpecification(criteria);
        return budgetRepository.count(specification);
    }

    /**
     * Function to convert {@link BudgetCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Budget> createSpecification(BudgetCriteria criteria) {
        Specification<Budget> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null && criteria.getId().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Budget_.id));
            }
            if (criteria.getBudgetAmount() != null && criteria.getBudgetAmount().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBudgetAmount(), Budget_.budgetAmount));
            }
            if (criteria.getStartDate() != null && criteria.getStartDate().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getStartDate(), Budget_.startDate));
            }
            if (criteria.getEndDate() != null && criteria.getEndDate().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEndDate(), Budget_.endDate));
            }
            if (criteria.getCreatedAt() != null && criteria.getCreatedAt().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Budget_.createdAt));
            }
            if (criteria.getUpdatedAt() != null && criteria.getUpdatedAt().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Budget_.updatedAt));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Budget_.status));
            }
            if (criteria.getCategoryId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getCategoryId(), root -> root.join(Budget_.category, JoinType.LEFT).get(Category_.id))
                );
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getUserId(), root -> root.join(Budget_.user, JoinType.LEFT).get(User_.id))
                );
            }
        }
        return specification;
    }
}
