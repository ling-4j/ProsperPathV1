package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Member;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.MemberRepository;
import com.mycompany.myapp.service.MemberService;
import com.mycompany.myapp.service.UserService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.Member}.
 */
@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberRepository memberRepository;

    private final UserService userService;

    public MemberServiceImpl(MemberRepository memberRepository, UserService userService) {
        this.memberRepository = memberRepository;
        this.userService = userService;
    }

    @Override
    public Member save(Member member) {
        LOG.debug("Request to save Member : {}", member);
        if (member.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> currentUser = userService.getUserWithAuthorities();
                currentUser.ifPresent(member::setUser);
            }
        }
        return memberRepository.save(member);
    }

    @Override
    public Member update(Member member) {
        LOG.debug("Request to update Member : {}", member);
        if (member.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> currentUser = userService.getUserWithAuthorities();
                currentUser.ifPresent(member::setUser);
            }
        }
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

    public Page<Member> findAllWithEagerRelationships(Pageable pageable) {
        return memberRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findOne(Long id) {
        LOG.debug("Request to get Member : {}", id);
        return memberRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Member : {}", id);
        memberRepository.deleteById(id);
    }
}
