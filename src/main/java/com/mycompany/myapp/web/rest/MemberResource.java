package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Budget;
import com.mycompany.myapp.domain.Member;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.MemberRepository;
import com.mycompany.myapp.service.MemberQueryService;
import com.mycompany.myapp.service.MemberService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.service.criteria.BudgetCriteria;
import com.mycompany.myapp.service.criteria.MemberCriteria;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Member}.
 */
@RestController
@RequestMapping("/api/members")
public class MemberResource {

    private static final Logger LOG = LoggerFactory.getLogger(MemberResource.class);

    private static final String ENTITY_NAME = "member";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MemberService memberService;

    private final MemberRepository memberRepository;

    private final MemberQueryService memberQueryService;

    private final UserService userService;

    public MemberResource(
        MemberService memberService,
        MemberRepository memberRepository,
        MemberQueryService memberQueryService,
        UserService userService
    ) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.memberQueryService = memberQueryService;
        this.userService = userService;
    }

    /**
     * {@code POST  /members} : Create a new member.
     *
     * @param member the member to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new member, or with status {@code 400 (Bad Request)} if the
     *         member has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) throws URISyntaxException {
        LOG.debug("REST request to save Member : {}", member);
        if (member.getId() != null) {
            throw new BadRequestAlertException("A new member cannot already have an ID", ENTITY_NAME, "idexists");
        }
        member = memberService.save(member);
        return ResponseEntity.created(new URI("/api/members/" + member.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, member.getId().toString()))
            .body(member);
    }

    /**
     * {@code PUT  /members/:id} : Updates an existing member.
     *
     * @param id     the id of the member to save.
     * @param member the member to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated member,
     *         or with status {@code 400 (Bad Request)} if the member is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the member
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Member member
    ) throws URISyntaxException {
        LOG.debug("REST request to update Member : {}, {}", id, member);
        if (member.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, member.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!memberRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        member = memberService.update(member);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, member.getId().toString()))
            .body(member);
    }

    /**
     * {@code PATCH  /members/:id} : Partial updates given fields of an existing
     * member, field will ignore if it is null
     *
     * @param id     the id of the member to save.
     * @param member the member to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated member,
     *         or with status {@code 400 (Bad Request)} if the member is not valid,
     *         or with status {@code 404 (Not Found)} if the member is not found,
     *         or with status {@code 500 (Internal Server Error)} if the member
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Member> partialUpdateMember(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Member member
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Member partially : {}, {}", id, member);
        if (member.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, member.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!memberRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Member> result = memberService.partialUpdate(member);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, member.getId().toString())
        );
    }

    /**
     * {@code GET  /members} : get all the members.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of members in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Member>> getAllMembers(
        MemberCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Members by criteria: {}", criteria);
        // Lấy người dùng hiện tại và xử lý nếu không tồn tại
        User currentUser = userService.getUserWithAuthorities().orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        // Gán userId của người dùng hiện tại vào criteria
        LongFilter userIdFilter = new LongFilter();
        userIdFilter.setEquals(currentUser.getId());
        criteria.setUserId(userIdFilter);
        Page<Member> page = memberQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /members/count} : count all the members.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countMembers(MemberCriteria criteria) {
        LOG.debug("REST request to count Members by criteria: {}", criteria);
        return ResponseEntity.ok().body(memberQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /members/:id} : get the "id" member.
     *
     * @param id the id of the member to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the member, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Member : {}", id);
        Optional<Member> member = memberService.findOne(id);
        return ResponseUtil.wrapOrNotFound(member);
    }

    /**
     * {@code DELETE  /members/:id} : delete the "id" member.
     *
     * @param id the id of the member to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Member : {}", id);
        memberService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
