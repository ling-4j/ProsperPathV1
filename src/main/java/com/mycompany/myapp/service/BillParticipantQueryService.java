package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.BillParticipantRepository;
import com.mycompany.myapp.service.criteria.BillParticipantCriteria;
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
 * Service for executing complex queries for {@link BillParticipant} entities in
 * the database.
 * The main input is a {@link BillParticipantCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link BillParticipant} which fulfills the
 * criteria.
 */
@Service
@Transactional(readOnly = true)
public class BillParticipantQueryService extends QueryService<BillParticipant> {

    private static final Logger LOG = LoggerFactory.getLogger(BillParticipantQueryService.class);

    private final BillParticipantRepository billParticipantRepository;

    public BillParticipantQueryService(BillParticipantRepository billParticipantRepository) {
        this.billParticipantRepository = billParticipantRepository;
    }

    /**
     * Return a {@link Page} of {@link BillParticipant} which matches the criteria
     * from the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BillParticipant> findByCriteria(BillParticipantCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<BillParticipant> specification = createSpecification(criteria);
        return billParticipantRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BillParticipantCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<BillParticipant> specification = createSpecification(criteria);
        return billParticipantRepository.count(specification);
    }

    /**
     * Function to convert {@link BillParticipantCriteria} to a
     * {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<BillParticipant> createSpecification(BillParticipantCriteria criteria) {
        Specification<BillParticipant> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null && criteria.getId().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), BillParticipant_.id));
            }
            if (criteria.getShareAmount() != null && criteria.getShareAmount().getEquals() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getShareAmount(), BillParticipant_.shareAmount));
            }
            if (criteria.getBillId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getBillId(), root -> root.join(BillParticipant_.bill, JoinType.LEFT).get(Bill_.id))
                );
            }
            if (criteria.getMemberId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getMemberId(), root -> root.join(BillParticipant_.member, JoinType.LEFT).get(Member_.id))
                );
            }
        }
        return specification;
    }
}
