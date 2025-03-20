package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.service.SummaryService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Summary}.
 */
@Service
@Transactional
public class SummaryServiceImpl implements SummaryService {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryServiceImpl.class);

    private final SummaryRepository summaryRepository;

    public SummaryServiceImpl(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Override
    public Summary save(Summary summary) {
        LOG.debug("Request to save Summary : {}", summary);
        return summaryRepository.save(summary);
    }

    @Override
    public Summary update(Summary summary) {
        LOG.debug("Request to update Summary : {}", summary);
        return summaryRepository.save(summary);
    }

    @Override
    public Optional<Summary> partialUpdate(Summary summary) {
        LOG.debug("Request to partially update Summary : {}", summary);

        return summaryRepository
            .findById(summary.getId())
            .map(existingSummary -> {
                if (summary.getPeriodType() != null) {
                    existingSummary.setPeriodType(summary.getPeriodType());
                }
                if (summary.getPeriodValue() != null) {
                    existingSummary.setPeriodValue(summary.getPeriodValue());
                }
                if (summary.getTotalAssets() != null) {
                    existingSummary.setTotalAssets(summary.getTotalAssets());
                }
                if (summary.getTotalIncome() != null) {
                    existingSummary.setTotalIncome(summary.getTotalIncome());
                }
                if (summary.getTotalExpense() != null) {
                    existingSummary.setTotalExpense(summary.getTotalExpense());
                }
                if (summary.getTotalProfit() != null) {
                    existingSummary.setTotalProfit(summary.getTotalProfit());
                }
                if (summary.getProfitPercentage() != null) {
                    existingSummary.setProfitPercentage(summary.getProfitPercentage());
                }
                if (summary.getCreatedAt() != null) {
                    existingSummary.setCreatedAt(summary.getCreatedAt());
                }
                if (summary.getUpdatedAt() != null) {
                    existingSummary.setUpdatedAt(summary.getUpdatedAt());
                }

                return existingSummary;
            })
            .map(summaryRepository::save);
    }

    public Page<Summary> findAllWithEagerRelationships(Pageable pageable) {
        return summaryRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Summary> findOne(Long id) {
        LOG.debug("Request to get Summary : {}", id);
        return summaryRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Summary : {}", id);
        summaryRepository.deleteById(id);
    }
}
