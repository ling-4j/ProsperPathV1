package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.TeamMember;
import com.mycompany.myapp.repository.TeamMemberRepository;
import com.mycompany.myapp.service.TeamMemberService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.TeamMember}.
 */
@Service
@Transactional
public class TeamMemberServiceImpl implements TeamMemberService {

    private static final Logger LOG = LoggerFactory.getLogger(TeamMemberServiceImpl.class);

    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberServiceImpl(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public TeamMember save(TeamMember teamMember) {
        LOG.debug("Request to save TeamMember : {}", teamMember);
        return teamMemberRepository.save(teamMember);
    }

    @Override
    public TeamMember update(TeamMember teamMember) {
        LOG.debug("Request to update TeamMember : {}", teamMember);
        return teamMemberRepository.save(teamMember);
    }

    @Override
    public Optional<TeamMember> partialUpdate(TeamMember teamMember) {
        LOG.debug("Request to partially update TeamMember : {}", teamMember);

        return teamMemberRepository
            .findById(teamMember.getId())
            .map(existingTeamMember -> {
                if (teamMember.getJoinedAt() != null) {
                    existingTeamMember.setJoinedAt(teamMember.getJoinedAt());
                }

                return existingTeamMember;
            })
            .map(teamMemberRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMember> findOne(Long id) {
        LOG.debug("Request to get TeamMember : {}", id);
        return teamMemberRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TeamMember : {}", id);
        teamMemberRepository.deleteById(id);
    }
}
