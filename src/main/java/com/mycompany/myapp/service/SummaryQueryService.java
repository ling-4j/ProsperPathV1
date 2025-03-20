package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.service.criteria.SummaryCriteria;
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
 * Service for executing complex queries for {@link Summary} entities in the database.
 * The main input is a {@link SummaryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Summary} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SummaryQueryService extends QueryService<Summary> {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryQueryService.class);

    private final SummaryRepository summaryRepository;

    public SummaryQueryService(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    public Summary getSummaryForPeriod(String period) {
        // Retrieve all summaries and filter by matching period (ignore case)
        // Return the one with the latest createdAt or a new Summary if none is found
        return summaryRepository
            .findAll()
            .stream()
            .filter(s -> s.getPeriodType() != null && s.getPeriodType().toString().equalsIgnoreCase(period))
            .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
            .orElse(new Summary());
    }

    /**
     * Return a {@link Page} of {@link Summary} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Summary> findByCriteria(SummaryCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Summary> specification = createSpecification(criteria);
        return summaryRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SummaryCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Summary> specification = createSpecification(criteria);
        return summaryRepository.count(specification);
    }

    /**
     * Function to convert {@link SummaryCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Summary> createSpecification(SummaryCriteria criteria) {
        Specification<Summary> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Summary_.id));
            }
            if (criteria.getPeriodType() != null) {
                specification = specification.and(buildSpecification(criteria.getPeriodType(), Summary_.periodType));
            }
            if (criteria.getPeriodValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPeriodValue(), Summary_.periodValue));
            }
            if (criteria.getTotalAssets() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalAssets(), Summary_.totalAssets));
            }
            if (criteria.getTotalIncome() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalIncome(), Summary_.totalIncome));
            }
            if (criteria.getTotalExpense() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalExpense(), Summary_.totalExpense));
            }
            if (criteria.getTotalProfit() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalProfit(), Summary_.totalProfit));
            }
            if (criteria.getProfitPercentage() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getProfitPercentage(), Summary_.profitPercentage));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Summary_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Summary_.updatedAt));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getUserId(), root -> root.join(Summary_.user, JoinType.LEFT).get(User_.id))
                );
            }
        }
        return specification;
    }
}
