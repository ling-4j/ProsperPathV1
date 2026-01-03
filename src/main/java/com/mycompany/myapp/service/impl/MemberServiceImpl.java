package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Member;
import com.mycompany.myapp.repository.MemberRepository;
import com.mycompany.myapp.service.MemberService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Member}.
 */
@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member save(Member member) {
        LOG.debug("Request to save Member : {}", member);
        return memberRepository.save(member);
    }

    @Override
    public Member update(Member member) {
        LOG.debug("Request to update Member : {}", member);
        return memberRepository.save(member);
    }

    @Override
    public Optional<Member> partialUpdate(Member member) {
        LOG.debug("Request to partially update Member : {}", member);

        return memberRepository
            .findById(member.getId())
            .map(existingMember -> {
                if (member.getName() != null) {
                    existingMember.setName(member.getName());
                }
                if (member.getNote() != null) {
                    existingMember.setNote(member.getNote());
                }
                if (member.getCreatedAt() != null) {
                    existingMember.setCreatedAt(member.getCreatedAt());
                }

                return existingMember;
            })
            .map(memberRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findOne(Long id) {
        LOG.debug("Request to get Member : {}", id);
        return memberRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Member : {}", id);
        memberRepository.deleteById(id);
    }
}
