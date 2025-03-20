package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.service.SummaryQueryService;
import com.mycompany.myapp.service.SummaryService;
import com.mycompany.myapp.service.criteria.SummaryCriteria;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Summary}.
 */
@RestController
@RequestMapping("/api/summaries")
public class SummaryResource {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryResource.class);

    private static final String ENTITY_NAME = "summary";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SummaryService summaryService;

    private final SummaryRepository summaryRepository;

    private final SummaryQueryService summaryQueryService;

    public SummaryResource(SummaryService summaryService, SummaryRepository summaryRepository, SummaryQueryService summaryQueryService) {
        this.summaryService = summaryService;
        this.summaryRepository = summaryRepository;
        this.summaryQueryService = summaryQueryService;
    }

    /**
     * {@code POST  /summaries} : Create a new summary.
     *
     * @param summary the summary to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new summary, or with status {@code 400 (Bad Request)} if the summary has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Summary> createSummary(@Valid @RequestBody Summary summary) throws URISyntaxException {
        LOG.debug("REST request to save Summary : {}", summary);
        if (summary.getId() != null) {
            throw new BadRequestAlertException("A new summary cannot already have an ID", ENTITY_NAME, "idexists");
        }
        summary = summaryService.save(summary);
        return ResponseEntity.created(new URI("/api/summaries/" + summary.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, summary.getId().toString()))
            .body(summary);
    }

    /**
     * {@code PUT  /summaries/:id} : Updates an existing summary.
     *
     * @param id the id of the summary to save.
     * @param summary the summary to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated summary,
     * or with status {@code 400 (Bad Request)} if the summary is not valid,
     * or with status {@code 500 (Internal Server Error)} if the summary couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Summary> updateSummary(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Summary summary
    ) throws URISyntaxException {
        LOG.debug("REST request to update Summary : {}, {}", id, summary);
        if (summary.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, summary.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!summaryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        summary = summaryService.update(summary);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, summary.getId().toString()))
            .body(summary);
    }

    /**
     * {@code PATCH  /summaries/:id} : Partial updates given fields of an existing summary, field will ignore if it is null
     *
     * @param id the id of the summary to save.
     * @param summary the summary to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated summary,
     * or with status {@code 400 (Bad Request)} if the summary is not valid,
     * or with status {@code 404 (Not Found)} if the summary is not found,
     * or with status {@code 500 (Internal Server Error)} if the summary couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Summary> partialUpdateSummary(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Summary summary
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Summary partially : {}, {}", id, summary);
        if (summary.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, summary.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!summaryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Summary> result = summaryService.partialUpdate(summary);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, summary.getId().toString())
        );
    }

    /**
     * {@code GET  /summaries} : get all the summaries.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of summaries in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Summary>> getAllSummaries(
        SummaryCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Summaries by criteria: {}", criteria);

        Page<Summary> page = summaryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /summaries/count} : count all the summaries.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSummaries(SummaryCriteria criteria) {
        LOG.debug("REST request to count Summaries by criteria: {}", criteria);
        return ResponseEntity.ok().body(summaryQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /summaries/:id} : get the "id" summary.
     *
     * @param id the id of the summary to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the summary, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Summary> getSummary(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Summary : {}", id);
        Optional<Summary> summary = summaryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(summary);
    }

    /**
     * {@code GET  /summary} : get the summary for a period.
     *
     * @param period the period for which to retrieve the summary.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the summary.
     */
    @GetMapping("/summary")
    public ResponseEntity<Summary> getSummary(@RequestParam("period") String period) {
        LOG.debug("REST request to get Summary for period: {}", period);
        Summary summary = summaryQueryService.getSummaryForPeriod(period);
        return ResponseEntity.ok(summary);
    }

    /**
     * {@code DELETE  /summaries/:id} : delete the "id" summary.
     *
     * @param id the id of the summary to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSummary(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Summary : {}", id);
        summaryService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
