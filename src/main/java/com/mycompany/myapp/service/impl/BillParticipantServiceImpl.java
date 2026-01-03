package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Bill;
import com.mycompany.myapp.domain.BillParticipant;
import com.mycompany.myapp.domain.Member;
import com.mycompany.myapp.domain.Member;
import com.mycompany.myapp.repository.BillParticipantRepository;
import com.mycompany.myapp.repository.BillRepository;
import com.mycompany.myapp.repository.MemberRepository;
import com.mycompany.myapp.service.BillParticipantService;
import com.mycompany.myapp.service.EventBalanceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.BillParticipant}.
 */
@Service
@Transactional
public class BillParticipantServiceImpl implements BillParticipantService {

    private static final Logger LOG = LoggerFactory.getLogger(BillParticipantServiceImpl.class);

    private final BillParticipantRepository billParticipantRepository;
    private final EventBalanceService eventBalanceService;
    private final BillRepository billRepository;
    private final MemberRepository memberRepository;

    public BillParticipantServiceImpl(
        BillParticipantRepository billParticipantRepository,
        EventBalanceService eventBalanceService,
        BillRepository billRepository,
        MemberRepository memberRepository
    ) {
        this.billParticipantRepository = billParticipantRepository;
        this.eventBalanceService = eventBalanceService;
        this.billRepository = billRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public BillParticipant save(BillParticipant billParticipant) {
        Long billId = billParticipant.getBill().getId();
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new IllegalArgumentException("Bill not found"));
        billParticipant.setBill(bill);
        BillParticipant result = billParticipantRepository.save(billParticipant);
        Long eventId = bill.getEvent().getId();
        eventBalanceService.recalculateByEvent(eventId);

        return result;
    }

    @Override
    public BillParticipant update(BillParticipant billParticipant) {
        LOG.debug("Request to update BillParticipant : {}", billParticipant);
        return billParticipantRepository.save(billParticipant);
    }

    @Override
    public Optional<BillParticipant> partialUpdate(BillParticipant billParticipant) {
        LOG.debug("Request to partially update BillParticipant : {}", billParticipant);

        return billParticipantRepository
            .findById(billParticipant.getId())
            .map(existingBillParticipant -> {
                if (billParticipant.getShareAmount() != null) {
                    existingBillParticipant.setShareAmount(billParticipant.getShareAmount());
                }

                return existingBillParticipant;
            })
            .map(billParticipantRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillParticipant> findOne(Long id) {
        LOG.debug("Request to get BillParticipant : {}", id);
        return billParticipantRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BillParticipant : {}", id);
        billParticipantRepository.deleteById(id);
    }

    @Transactional
    public void saveBillParticipants(Long billId, List<Long> memberIds) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        billParticipantRepository.deleteByBillId(billId);

        int count = memberIds.size();
        BigDecimal share = count > 0 ? bill.getAmount().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        for (Long memberId : memberIds) {
            BillParticipant bp = new BillParticipant();
            bp.setBill(bill);
            bp.setMember(memberRepository.getReferenceById(memberId)); // <-- đây mới đúng
            bp.setShareAmount(share);
            billParticipantRepository.save(bp);
        }

        eventBalanceService.recalculateByEvent(bill.getEvent().getId());
    }
}
