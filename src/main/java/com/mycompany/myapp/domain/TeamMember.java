package com.mycompany.myapp.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A TeamMember.
 */
@Entity
@Table(name = "team_member")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    private Team team;

    @ManyToOne(fetch = FetchType.EAGER)
    private Member member;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TeamMember id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getJoinedAt() {
        return this.joinedAt;
    }

    public TeamMember joinedAt(Instant joinedAt) {
        this.setJoinedAt(joinedAt);
        return this;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public TeamMember team(Team team) {
        this.setTeam(team);
        return this;
    }

    public Member getMember() {
        return this.member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public TeamMember member(Member member) {
        this.setMember(member);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeamMember)) {
            return false;
        }
        return getId() != null && getId().equals(((TeamMember) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TeamMember{" +
            "id=" + getId() +
            ", joinedAt='" + getJoinedAt() + "'" +
            "}";
    }
}
