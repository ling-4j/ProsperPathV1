package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.EventBalanceRepository;
import com.mycompany.myapp.service.criteria.EventBalanceCriteria;
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
 * Service for executing complex queries for {@link EventBalance} entities in
 * the database.
 * The main input is a {@link EventBalanceCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link EventBalance} which fulfills the
 * criteria.
 */
@Service
@Transactional(readOnly = true)
public class EventBalanceQueryService extends QueryService<EventBalance> {

    private static final Logger LOG = LoggerFactory.getLogger(EventBalanceQueryService.class);

    private final EventBalanceRepository eventBalanceRepository;

    public EventBalanceQueryService(EventBalanceRepository eventBalanceRepository) {
        this.eventBalanceRepository = eventBalanceRepository;
    }

    /**
     * Return a {@link Page} of {@link EventBalance} which matches the criteria from
     * the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EventBalance> findByCriteria(EventBalanceCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<EventBalance> specification = createSpecification(criteria);
        return eventBalanceRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EventBalanceCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<EventBalance> specification = createSpecification(criteria);
        return eventBalanceRepository.count(specification);
    }

    /**
     * Function to convert {@link EventBalanceCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<EventBalance> createSpecification(EventBalanceCriteria criteria) {
        Specification<EventBalance> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null && criteria.getId().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), EventBalance_.id));
            }
            if (criteria.getPaid() != null && criteria.getPaid().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPaid(), EventBalance_.paid));
            }
            if (criteria.getShouldPay() != null && criteria.getShouldPay().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getShouldPay(), EventBalance_.shouldPay));
            }
            if (criteria.getBalance() != null && criteria.getBalance().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBalance(), EventBalance_.balance));
            }
            if (criteria.getEventId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getEventId(), root -> root.join(EventBalance_.event, JoinType.LEFT).get(Event_.id))
                );
            }
            if (criteria.getMemberId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getMemberId(), root -> root.join(EventBalance_.member, JoinType.LEFT).get(Member_.id))
                );
            }
        }
        return specification;
    }
}
