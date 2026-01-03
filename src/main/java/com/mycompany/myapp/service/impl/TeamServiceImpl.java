package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Team;
import com.mycompany.myapp.repository.TeamRepository;
import com.mycompany.myapp.service.TeamService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Team}.
 */
@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private static final Logger LOG = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team save(Team team) {
        LOG.debug("Request to save Team : {}", team);
        return teamRepository.save(team);
    }

    @Override
    public Team update(Team team) {
        LOG.debug("Request to update Team : {}", team);
        return teamRepository.save(team);
    }

    @Override
    public Optional<Team> partialUpdate(Team team) {
        LOG.debug("Request to partially update Team : {}", team);

        return teamRepository
            .findById(team.getId())
            .map(existingTeam -> {
                if (team.getName() != null) {
                    existingTeam.setName(team.getName());
                }
                if (team.getCreatedAt() != null) {
                    existingTeam.setCreatedAt(team.getCreatedAt());
                }

                return existingTeam;
            })
            .map(teamRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Team> findOne(Long id) {
        LOG.debug("Request to get Team : {}", id);
        return teamRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Team : {}", id);
        teamRepository.deleteById(id);
    }
}
