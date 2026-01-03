package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Bill;
import com.mycompany.myapp.repository.BillRepository;
import com.mycompany.myapp.service.BillService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Bill}.
 */
@Service
@Transactional
public class BillServiceImpl implements BillService {

    private static final Logger LOG = LoggerFactory.getLogger(BillServiceImpl.class);

    private final BillRepository billRepository;

    public BillServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    public Bill save(Bill bill) {
        LOG.debug("Request to save Bill : {}", bill);
        return billRepository.save(bill);
    }

    @Override
    public Bill update(Bill bill) {
        LOG.debug("Request to update Bill : {}", bill);
        return billRepository.save(bill);
    }

    @Override
    public Optional<Bill> partialUpdate(Bill bill) {
        LOG.debug("Request to partially update Bill : {}", bill);

        return billRepository
            .findById(bill.getId())
            .map(existingBill -> {
                if (bill.getName() != null) {
                    existingBill.setName(bill.getName());
                }
                if (bill.getAmount() != null) {
                    existingBill.setAmount(bill.getAmount());
                }
                if (bill.getCreatedAt() != null) {
                    existingBill.setCreatedAt(bill.getCreatedAt());
                }

                return existingBill;
            })
            .map(billRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bill> findOne(Long id) {
        LOG.debug("Request to get Bill : {}", id);
        return billRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Bill : {}", id);
        billRepository.deleteById(id);
    }
}
