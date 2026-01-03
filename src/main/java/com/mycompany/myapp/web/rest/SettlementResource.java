package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Settlement;
import com.mycompany.myapp.repository.SettlementRepository;
import com.mycompany.myapp.service.SettlementQueryService;
import com.mycompany.myapp.service.SettlementService;
import com.mycompany.myapp.service.criteria.SettlementCriteria;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Settlement}.
 */
@RestController
@RequestMapping("/api/settlements")
public class SettlementResource {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementResource.class);

    private static final String ENTITY_NAME = "settlement";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SettlementService settlementService;

    private final SettlementRepository settlementRepository;

    private final SettlementQueryService settlementQueryService;

    public SettlementResource(
        SettlementService settlementService,
        SettlementRepository settlementRepository,
        SettlementQueryService settlementQueryService
    ) {
        this.settlementService = settlementService;
        this.settlementRepository = settlementRepository;
        this.settlementQueryService = settlementQueryService;
    }

    /**
     * {@code POST  /settlements} : Create a new settlement.
     *
     * @param settlement the settlement to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new settlement, or with status {@code 400 (Bad Request)} if the settlement has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Settlement> createSettlement(@Valid @RequestBody Settlement settlement) throws URISyntaxException {
        LOG.debug("REST request to save Settlement : {}", settlement);
        if (settlement.getId() != null) {
            throw new BadRequestAlertException("A new settlement cannot already have an ID", ENTITY_NAME, "idexists");
        }
        settlement = settlementService.save(settlement);
        return ResponseEntity.created(new URI("/api/settlements/" + settlement.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, settlement.getId().toString()))
            .body(settlement);
    }

    /**
     * {@code PUT  /settlements/:id} : Updates an existing settlement.
     *
     * @param id the id of the settlement to save.
     * @param settlement the settlement to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated settlement,
     * or with status {@code 400 (Bad Request)} if the settlement is not valid,
     * or with status {@code 500 (Internal Server Error)} if the settlement couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Settlement> updateSettlement(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Settlement settlement
    ) throws URISyntaxException {
        LOG.debug("REST request to update Settlement : {}, {}", id, settlement);
        if (settlement.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, settlement.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!settlementRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        settlement = settlementService.update(settlement);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, settlement.getId().toString()))
            .body(settlement);
    }

    /**
     * {@code PATCH  /settlements/:id} : Partial updates given fields of an existing settlement, field will ignore if it is null
     *
     * @param id the id of the settlement to save.
     * @param settlement the settlement to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated settlement,
     * or with status {@code 400 (Bad Request)} if the settlement is not valid,
     * or with status {@code 404 (Not Found)} if the settlement is not found,
     * or with status {@code 500 (Internal Server Error)} if the settlement couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Settlement> partialUpdateSettlement(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Settlement settlement
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Settlement partially : {}, {}", id, settlement);
        if (settlement.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, settlement.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!settlementRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Settlement> result = settlementService.partialUpdate(settlement);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, settlement.getId().toString())
        );
    }

    /**
     * {@code GET  /settlements} : get all the settlements.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of settlements in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Settlement>> getAllSettlements(
        SettlementCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Settlements by criteria: {}", criteria);

        Page<Settlement> page = settlementQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /settlements/count} : count all the settlements.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSettlements(SettlementCriteria criteria) {
        LOG.debug("REST request to count Settlements by criteria: {}", criteria);
        return ResponseEntity.ok().body(settlementQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /settlements/:id} : get the "id" settlement.
     *
     * @param id the id of the settlement to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the settlement, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Settlement> getSettlement(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Settlement : {}", id);
        Optional<Settlement> settlement = settlementService.findOne(id);
        return ResponseUtil.wrapOrNotFound(settlement);
    }

    /**
     * {@code DELETE  /settlements/:id} : delete the "id" settlement.
     *
     * @param id the id of the settlement to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSettlement(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Settlement : {}", id);
        settlementService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
