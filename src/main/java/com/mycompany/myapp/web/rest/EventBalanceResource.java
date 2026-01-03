package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.EventBalance;
import com.mycompany.myapp.repository.EventBalanceRepository;
import com.mycompany.myapp.service.EventBalanceQueryService;
import com.mycompany.myapp.service.EventBalanceService;
import com.mycompany.myapp.service.criteria.EventBalanceCriteria;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.EventBalance}.
 */
@RestController
@RequestMapping("/api/event-balances")
public class EventBalanceResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventBalanceResource.class);

    private static final String ENTITY_NAME = "eventBalance";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventBalanceService eventBalanceService;

    private final EventBalanceRepository eventBalanceRepository;

    private final EventBalanceQueryService eventBalanceQueryService;

    public EventBalanceResource(
        EventBalanceService eventBalanceService,
        EventBalanceRepository eventBalanceRepository,
        EventBalanceQueryService eventBalanceQueryService
    ) {
        this.eventBalanceService = eventBalanceService;
        this.eventBalanceRepository = eventBalanceRepository;
        this.eventBalanceQueryService = eventBalanceQueryService;
    }

    /**
     * {@code POST  /event-balances} : Create a new eventBalance.
     *
     * @param eventBalance the eventBalance to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventBalance, or with status {@code 400 (Bad Request)} if the eventBalance has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventBalance> createEventBalance(@Valid @RequestBody EventBalance eventBalance) throws URISyntaxException {
        LOG.debug("REST request to save EventBalance : {}", eventBalance);
        if (eventBalance.getId() != null) {
            throw new BadRequestAlertException("A new eventBalance cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventBalance = eventBalanceService.save(eventBalance);
        return ResponseEntity.created(new URI("/api/event-balances/" + eventBalance.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, eventBalance.getId().toString()))
            .body(eventBalance);
    }

    /**
     * {@code PUT  /event-balances/:id} : Updates an existing eventBalance.
     *
     * @param id the id of the eventBalance to save.
     * @param eventBalance the eventBalance to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventBalance,
     * or with status {@code 400 (Bad Request)} if the eventBalance is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventBalance couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventBalance> updateEventBalance(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventBalance eventBalance
    ) throws URISyntaxException {
        LOG.debug("REST request to update EventBalance : {}, {}", id, eventBalance);
        if (eventBalance.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventBalance.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventBalanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventBalance = eventBalanceService.update(eventBalance);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventBalance.getId().toString()))
            .body(eventBalance);
    }

    /**
     * {@code PATCH  /event-balances/:id} : Partial updates given fields of an existing eventBalance, field will ignore if it is null
     *
     * @param id the id of the eventBalance to save.
     * @param eventBalance the eventBalance to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventBalance,
     * or with status {@code 400 (Bad Request)} if the eventBalance is not valid,
     * or with status {@code 404 (Not Found)} if the eventBalance is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventBalance couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventBalance> partialUpdateEventBalance(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventBalance eventBalance
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update EventBalance partially : {}, {}", id, eventBalance);
        if (eventBalance.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventBalance.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventBalanceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventBalance> result = eventBalanceService.partialUpdate(eventBalance);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventBalance.getId().toString())
        );
    }

    /**
     * {@code GET  /event-balances} : get all the eventBalances.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventBalances in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EventBalance>> getAllEventBalances(
        EventBalanceCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get EventBalances by criteria: {}", criteria);

        Page<EventBalance> page = eventBalanceQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /event-balances/count} : count all the eventBalances.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEventBalances(EventBalanceCriteria criteria) {
        LOG.debug("REST request to count EventBalances by criteria: {}", criteria);
        return ResponseEntity.ok().body(eventBalanceQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /event-balances/:id} : get the "id" eventBalance.
     *
     * @param id the id of the eventBalance to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventBalance, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventBalance> getEventBalance(@PathVariable("id") Long id) {
        LOG.debug("REST request to get EventBalance : {}", id);
        Optional<EventBalance> eventBalance = eventBalanceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventBalance);
    }

    /**
     * {@code DELETE  /event-balances/:id} : delete the "id" eventBalance.
     *
     * @param id the id of the eventBalance to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventBalance(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete EventBalance : {}", id);
        eventBalanceService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
