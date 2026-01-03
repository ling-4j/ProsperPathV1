package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Bill;
import com.mycompany.myapp.domain.BillParticipant;
import com.mycompany.myapp.domain.Event;
import com.mycompany.myapp.domain.EventBalance;
import com.mycompany.myapp.domain.Member;
import com.mycompany.myapp.repository.BillParticipantRepository;
import com.mycompany.myapp.repository.BillRepository;
import com.mycompany.myapp.repository.EventBalanceRepository;
import com.mycompany.myapp.repository.EventRepository;
import com.mycompany.myapp.service.EventBalanceService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.EventBalance}.
 */
@Service
@Transactional
public class EventBalanceServiceImpl implements EventBalanceService {

    private static final Logger LOG = LoggerFactory.getLogger(EventBalanceServiceImpl.class);

    private final EventBalanceRepository eventBalanceRepository;
    private final BillRepository billRepository;
    private final BillParticipantRepository billParticipantRepository;
    private final EventRepository eventRepository;

    public EventBalanceServiceImpl(
        EventBalanceRepository eventBalanceRepository,
        BillRepository billRepository,
        BillParticipantRepository billParticipantRepository,
        EventRepository eventRepository
    ) {
        this.eventBalanceRepository = eventBalanceRepository;
        this.billRepository = billRepository;
        this.billParticipantRepository = billParticipantRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public EventBalance save(EventBalance eventBalance) {
        LOG.debug("Request to save EventBalance : {}", eventBalance);
        return eventBalanceRepository.save(eventBalance);
    }

    @Override
    public EventBalance update(EventBalance eventBalance) {
        LOG.debug("Request to update EventBalance : {}", eventBalance);
        return eventBalanceRepository.save(eventBalance);
    }

    @Override
    public Optional<EventBalance> partialUpdate(EventBalance eventBalance) {
        LOG.debug("Request to partially update EventBalance : {}", eventBalance);

        return eventBalanceRepository
            .findById(eventBalance.getId())
            .map(existingEventBalance -> {
                if (eventBalance.getPaid() != null) {
                    existingEventBalance.setPaid(eventBalance.getPaid());
                }
                if (eventBalance.getShouldPay() != null) {
                    existingEventBalance.setShouldPay(eventBalance.getShouldPay());
                }
                if (eventBalance.getBalance() != null) {
                    existingEventBalance.setBalance(eventBalance.getBalance());
                }

                return existingEventBalance;
            })
            .map(eventBalanceRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventBalance> findOne(Long id) {
        LOG.debug("Request to get EventBalance : {}", id);
        return eventBalanceRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete EventBalance : {}", id);
        eventBalanceRepository.deleteById(id);
    }

    @Override
    public void recalculateByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));

        eventBalanceRepository.deleteByEventId(eventId);

        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> shouldPayMap = new HashMap<>();

        List<Bill> bills = billRepository.findAllByEventId(eventId);

        for (Bill bill : bills) {
            Long payerId = bill.getPayer().getId();
            BigDecimal amount = bill.getAmount();

            paidMap.merge(payerId, amount, BigDecimal::add);

            List<BillParticipant> participants = billParticipantRepository.findAllByBillId(bill.getId());

            for (BillParticipant bp : participants) {
                Long memberId = bp.getMember().getId();
                shouldPayMap.merge(memberId, bp.getShareAmount(), BigDecimal::add);
            }
        }

        Set<Long> memberIds = new HashSet<>();
        memberIds.addAll(paidMap.keySet());
        memberIds.addAll(shouldPayMap.keySet());

        for (Long memberId : memberIds) {
            BigDecimal paid = paidMap.getOrDefault(memberId, BigDecimal.ZERO);
            BigDecimal shouldPay = shouldPayMap.getOrDefault(memberId, BigDecimal.ZERO);

            EventBalance eb = new EventBalance();
            eb.setEvent(event);

            Member member = new Member();
            member.setId(memberId);
            eb.setMember(member);

            eb.setPaid(paid);
            eb.setShouldPay(shouldPay);
            eb.setBalance(paid.subtract(shouldPay));

            eventBalanceRepository.save(eb);
        }
    }
}
