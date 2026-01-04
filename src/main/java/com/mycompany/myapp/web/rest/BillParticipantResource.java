package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.BillParticipant;
import com.mycompany.myapp.repository.BillParticipantRepository;
import com.mycompany.myapp.service.BillParticipantQueryService;
import com.mycompany.myapp.service.BillParticipantService;
import com.mycompany.myapp.service.criteria.BillParticipantCriteria;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
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
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing
 * {@link com.mycompany.myapp.domain.BillParticipant}.
 */
@RestController
@RequestMapping("/api/bill-participants")
public class BillParticipantResource {

    private static final Logger LOG = LoggerFactory.getLogger(BillParticipantResource.class);

    private static final String ENTITY_NAME = "billParticipant";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BillParticipantService billParticipantService;

    private final BillParticipantRepository billParticipantRepository;

    private final BillParticipantQueryService billParticipantQueryService;

    public BillParticipantResource(
        BillParticipantService billParticipantService,
        BillParticipantRepository billParticipantRepository,
        BillParticipantQueryService billParticipantQueryService
    ) {
        this.billParticipantService = billParticipantService;
        this.billParticipantRepository = billParticipantRepository;
        this.billParticipantQueryService = billParticipantQueryService;
    }

    /**
     * {@code POST  /bill-participants} : Create a new billParticipant.
     *
     * @param billParticipant the billParticipant to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new billParticipant, or with status
     *         {@code 400 (Bad Request)} if the billParticipant has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BillParticipant> createBillParticipant(@Valid @RequestBody BillParticipant billParticipant)
        throws URISyntaxException {
        LOG.debug("REST request to save BillParticipant : {}", billParticipant);
        if (billParticipant.getId() != null) {
            throw new BadRequestAlertException("A new billParticipant cannot already have an ID", ENTITY_NAME, "idexists");
        }
        billParticipant = billParticipantService.save(billParticipant);
        return ResponseEntity.created(new URI("/api/bill-participants/" + billParticipant.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, billParticipant.getId().toString()))
            .body(billParticipant);
    }

    @PostMapping("/save-bill-participants/{billId}")
    public ResponseEntity<Void> saveBillParticipants(@PathVariable Long billId, @RequestBody List<Long> memberIds) {
        billParticipantService.saveBillParticipants(billId, memberIds);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code PUT  /bill-participants/:id} : Updates an existing billParticipant.
     *
     * @param id              the id of the billParticipant to save.
     * @param billParticipant the billParticipant to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated billParticipant,
     *         or with status {@code 400 (Bad Request)} if the billParticipant is
     *         not valid,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         billParticipant couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BillParticipant> updateBillParticipant(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BillParticipant billParticipant
    ) throws URISyntaxException {
        LOG.debug("REST request to update BillParticipant : {}, {}", id, billParticipant);
        if (billParticipant.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, billParticipant.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!billParticipantRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        billParticipant = billParticipantService.update(billParticipant);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, billParticipant.getId().toString()))
            .body(billParticipant);
    }

    /**
     * {@code PATCH  /bill-participants/:id} : Partial updates given fields of an
     * existing billParticipant, field will ignore if it is null
     *
     * @param id              the id of the billParticipant to save.
     * @param billParticipant the billParticipant to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated billParticipant,
     *         or with status {@code 400 (Bad Request)} if the billParticipant is
     *         not valid,
     *         or with status {@code 404 (Not Found)} if the billParticipant is not
     *         found,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         billParticipant couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BillParticipant> partialUpdateBillParticipant(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BillParticipant billParticipant
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BillParticipant partially : {}, {}", id, billParticipant);
        if (billParticipant.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, billParticipant.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!billParticipantRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BillParticipant> result = billParticipantService.partialUpdate(billParticipant);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, billParticipant.getId().toString())
        );
    }

    /**
     * {@code GET  /bill-participants} : get all the billParticipants.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of billParticipants in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BillParticipant>> getAllBillParticipants(
        BillParticipantCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get BillParticipants by criteria: {}", criteria);

        Page<BillParticipant> page = billParticipantQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /bill-participants/count} : count all the billParticipants.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countBillParticipants(BillParticipantCriteria criteria) {
        LOG.debug("REST request to count BillParticipants by criteria: {}", criteria);
        return ResponseEntity.ok().body(billParticipantQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /bill-participants/:id} : get the "id" billParticipant.
     *
     * @param id the id of the billParticipant to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the billParticipant, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BillParticipant> getBillParticipant(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BillParticipant : {}", id);
        Optional<BillParticipant> billParticipant = billParticipantService.findOne(id);
        return ResponseUtil.wrapOrNotFound(billParticipant);
    }

    /**
     * {@code DELETE  /bill-participants/:id} : delete the "id" billParticipant.
     *
     * @param id the id of the billParticipant to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBillParticipant(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BillParticipant : {}", id);
        billParticipantService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
