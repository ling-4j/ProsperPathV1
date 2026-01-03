package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.BillRepository;
import com.mycompany.myapp.service.criteria.BillCriteria;
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
 * Service for executing complex queries for {@link Bill} entities in the
 * database.
 * The main input is a {@link BillCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Bill} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BillQueryService extends QueryService<Bill> {

    private static final Logger LOG = LoggerFactory.getLogger(BillQueryService.class);

    private final BillRepository billRepository;

    public BillQueryService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * Return a {@link Page} of {@link Bill} which matches the criteria from the
     * database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Bill> findByCriteria(BillCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Bill> specification = createSpecification(criteria);
        return billRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BillCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Bill> specification = createSpecification(criteria);
        return billRepository.count(specification);
    }

    /**
     * Function to convert {@link BillCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Bill> createSpecification(BillCriteria criteria) {
        Specification<Bill> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null && criteria.getId().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Bill_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Bill_.name));
            }
            if (criteria.getAmount() != null && criteria.getAmount().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Bill_.amount));
            }
            if (criteria.getCreatedAt() != null && criteria.getCreatedAt().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Bill_.createdAt));
            }
            if (criteria.getEventId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getEventId(), root -> root.join(Bill_.event, JoinType.LEFT).get(Event_.id))
                );
            }
            if (criteria.getPayerId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getPayerId(), root -> root.join(Bill_.payer, JoinType.LEFT).get(Member_.id))
                );
            }
        }
        return specification;
    }
}
