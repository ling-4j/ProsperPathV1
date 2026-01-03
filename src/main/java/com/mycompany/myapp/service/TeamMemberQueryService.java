package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.repository.TeamMemberRepository;
import com.mycompany.myapp.service.criteria.TeamMemberCriteria;
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
 * Service for executing complex queries for {@link TeamMember} entities in the
 * database.
 * The main input is a {@link TeamMemberCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TeamMember} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TeamMemberQueryService extends QueryService<TeamMember> {

    private static final Logger LOG = LoggerFactory.getLogger(TeamMemberQueryService.class);

    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberQueryService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    /**
     * Return a {@link Page} of {@link TeamMember} which matches the criteria from
     * the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TeamMember> findByCriteria(TeamMemberCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<TeamMember> specification = createSpecification(criteria);
        return teamMemberRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TeamMemberCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<TeamMember> specification = createSpecification(criteria);
        return teamMemberRepository.count(specification);
    }

    /**
     * Function to convert {@link TeamMemberCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<TeamMember> createSpecification(TeamMemberCriteria criteria) {
        Specification<TeamMember> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), TeamMember_.id));
            }
            if (criteria.getJoinedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getJoinedAt(), TeamMember_.joinedAt));
            }
            if (criteria.getTeamId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getTeamId(), root -> root.join(TeamMember_.team, JoinType.LEFT).get(Team_.id))
                );
            }
            if (criteria.getMemberId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getMemberId(), root -> root.join(TeamMember_.member, JoinType.LEFT).get(Member_.id))
                );
            }
        }
        return specification;
    }
}
