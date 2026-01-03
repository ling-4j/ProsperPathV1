package com.mycompany.myapp.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.TeamMember} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.TeamMemberResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /team-members?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TeamMemberCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter joinedAt;

    private LongFilter teamId;

    private LongFilter memberId;

    private Boolean distinct;

    public TeamMemberCriteria() {}

    public TeamMemberCriteria(TeamMemberCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.joinedAt = other.optionalJoinedAt().map(InstantFilter::copy).orElse(null);
        this.teamId = other.optionalTeamId().map(LongFilter::copy).orElse(null);
        this.memberId = other.optionalMemberId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TeamMemberCriteria copy() {
        return new TeamMemberCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public InstantFilter getJoinedAt() {
        return joinedAt;
    }

    public Optional<InstantFilter> optionalJoinedAt() {
        return Optional.ofNullable(joinedAt);
    }

    public InstantFilter joinedAt() {
        if (joinedAt == null) {
            setJoinedAt(new InstantFilter());
        }
        return joinedAt;
    }

    public void setJoinedAt(InstantFilter joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LongFilter getTeamId() {
        return teamId;
    }

    public Optional<LongFilter> optionalTeamId() {
        return Optional.ofNullable(teamId);
    }

    public LongFilter teamId() {
        if (teamId == null) {
            setTeamId(new LongFilter());
        }
        return teamId;
    }

    public void setTeamId(LongFilter teamId) {
        this.teamId = teamId;
    }

    public LongFilter getMemberId() {
        return memberId;
    }

    public Optional<LongFilter> optionalMemberId() {
        return Optional.ofNullable(memberId);
    }

    public LongFilter memberId() {
        if (memberId == null) {
            setMemberId(new LongFilter());
        }
        return memberId;
    }

    public void setMemberId(LongFilter memberId) {
        this.memberId = memberId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TeamMemberCriteria that = (TeamMemberCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(joinedAt, that.joinedAt) &&
            Objects.equals(teamId, that.teamId) &&
            Objects.equals(memberId, that.memberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, joinedAt, teamId, memberId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TeamMemberCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalJoinedAt().map(f -> "joinedAt=" + f + ", ").orElse("") +
            optionalTeamId().map(f -> "teamId=" + f + ", ").orElse("") +
            optionalMemberId().map(f -> "memberId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
