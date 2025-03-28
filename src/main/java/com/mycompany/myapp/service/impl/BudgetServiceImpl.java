package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Budget;
import com.mycompany.myapp.repository.BudgetRepository;
import com.mycompany.myapp.service.BudgetService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Budget}.
 */
@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private static final Logger LOG = LoggerFactory.getLogger(BudgetServiceImpl.class);

    private final BudgetRepository budgetRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget save(Budget budget) {
        LOG.debug("Request to save Budget : {}", budget);
        return budgetRepository.save(budget);
    }

    @Override
    public Budget update(Budget budget) {
        LOG.debug("Request to update Budget : {}", budget);
        return budgetRepository.save(budget);
    }

    @Override
    public Optional<Budget> partialUpdate(Budget budget) {
        LOG.debug("Request to partially update Budget : {}", budget);

        return budgetRepository
            .findById(budget.getId())
            .map(existingBudget -> {
                if (budget.getBudgetAmount() != null) {
                    existingBudget.setBudgetAmount(budget.getBudgetAmount());
                }
                if (budget.getStartDate() != null) {
                    existingBudget.setStartDate(budget.getStartDate());
                }
                if (budget.getEndDate() != null) {
                    existingBudget.setEndDate(budget.getEndDate());
                }
                if (budget.getCreatedAt() != null) {
                    existingBudget.setCreatedAt(budget.getCreatedAt());
                }
                if (budget.getUpdatedAt() != null) {
                    existingBudget.setUpdatedAt(budget.getUpdatedAt());
                }

                return existingBudget;
            })
            .map(budgetRepository::save);
    }

    public Page<Budget> findAllWithEagerRelationships(Pageable pageable) {
        return budgetRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Budget> findOne(Long id) {
        LOG.debug("Request to get Budget : {}", id);
        return budgetRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Budget : {}", id);
        budgetRepository.deleteById(id);
    }
}
