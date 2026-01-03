package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Settlement;
import com.mycompany.myapp.repository.SettlementRepository;
import com.mycompany.myapp.service.SettlementService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Settlement}.
 */
@Service
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementServiceImpl.class);

    private final SettlementRepository settlementRepository;

    public SettlementServiceImpl(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    @Override
    public Settlement save(Settlement settlement) {
        LOG.debug("Request to save Settlement : {}", settlement);
        return settlementRepository.save(settlement);
    }

    @Override
    public Settlement update(Settlement settlement) {
        LOG.debug("Request to update Settlement : {}", settlement);
        return settlementRepository.save(settlement);
    }

    @Override
    public Optional<Settlement> partialUpdate(Settlement settlement) {
        LOG.debug("Request to partially update Settlement : {}", settlement);

        return settlementRepository
            .findById(settlement.getId())
            .map(existingSettlement -> {
                if (settlement.getAmount() != null) {
                    existingSettlement.setAmount(settlement.getAmount());
                }

                return existingSettlement;
            })
            .map(settlementRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Settlement> findOne(Long id) {
        LOG.debug("Request to get Settlement : {}", id);
        return settlementRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Settlement : {}", id);
        settlementRepository.deleteById(id);
    }
}
