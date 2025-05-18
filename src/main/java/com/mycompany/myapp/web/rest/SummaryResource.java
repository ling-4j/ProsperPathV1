package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.service.SummaryQueryService;
import com.mycompany.myapp.service.SummaryService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.service.criteria.SummaryCriteria;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private final UserService userService;

    public SummaryResource(
            SummaryService summaryService,
            SummaryRepository summaryRepository,
            SummaryQueryService summaryQueryService,
            UserService userService) {
        this.summaryService = summaryService;
        this.summaryRepository = summaryRepository;
        this.summaryQueryService = summaryQueryService;
        this.userService = userService;
    }

    /**
     * {@code POST  /summaries} : Create a new summary.
     *
     * @param summary the summary to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new summary, or with status {@code 400 (Bad Request)} if the
     *         summary has already an ID.
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
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        summary.getId().toString()))
                .body(summary);
    }

    /**
     * {@code PUT  /summaries/:id} : Updates an existing summary.
     *
     * @param id      the id of the summary to save.
     * @param summary the summary to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated summary,
     *         or with status {@code 400 (Bad Request)} if the summary is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the summary
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Summary> updateSummary(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody Summary summary) throws URISyntaxException {
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
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        summary.getId().toString()))
                .body(summary);
    }

    /**
     * {@code PATCH  /summaries/:id} : Partial updates given fields of an existing
     * summary, field will ignore if it is null
     *
     * @param id      the id of the summary to save.
     * @param summary the summary to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated summary,
     *         or with status {@code 400 (Bad Request)} if the summary is not valid,
     *         or with status {@code 404 (Not Found)} if the summary is not found,
     *         or with status {@code 500 (Internal Server Error)} if the summary
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Summary> partialUpdateSummary(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody Summary summary) throws URISyntaxException {
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
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, summary.getId().toString()));
    }

    /**
     * {@code GET  /summaries} : get all the summaries.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of summaries in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Summary>> getAllSummaries(
            SummaryCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get Summaries by criteria: {}", criteria);

        Page<Summary> page = summaryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /summaries/count} : count all the summaries.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
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
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the summary, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Summary> getSummary(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Summary : {}", id);
        Optional<Summary> summary = summaryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(summary);
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

    /**
     * {@code GET /summary} : get the summary for a period.
     *
     * @param period the period for which to retrieve the summary (week, month,
     *               year).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the summary,
     *         or with status {@code 404 (Not Found)} if no summary is found.
     */
    @GetMapping("/summary")
    public ResponseEntity<Summary> getSummary(@RequestParam("period") String period) {
        LOG.debug("REST request to get Summary for period: {}", period);

        // Lấy người dùng hiện tại và xử lý nếu không tồn tại
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        Long userId = currentUser.getId();

        // Xác định periodValue dựa trên period
        LocalDate now = LocalDate.now();
        String periodValue = getPeriodValue(now, period.toUpperCase());

        // Lấy Summary cho kỳ hiện tại
        Summary summary = summaryQueryService.getSummaryForPeriod(userId, period.toUpperCase(), periodValue);
        if (summary == null) {
            return ResponseEntity.noContent().build();
        }

        // Tạo header tùy chỉnh
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Income", summary.getTotalIncome().toString());
        headers.add("X-Total-Expense", summary.getTotalExpense().toString());
        headers.add("X-Total-Profit", summary.getTotalProfit().toString());

        return ResponseEntity.ok().headers(headers).body(summary);
    }

    /**
     * {@code GET /financial-change} : get the financial change percentages for a
     * period.
     *
     * @param period the period for which to retrieve the financial changes (week,
     *               month, year).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the financial change percentages.
     */
    @GetMapping("/financial-change")
    public ResponseEntity<SummaryQueryService.FinancialChange> getFinancialChange(
            @RequestParam("period") String period) {
        LOG.debug("REST request to get FinancialChange for period: {}", period);
        // Lấy người dùng hiện tại và xử lý nếu không tồn tại
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        Long userId = currentUser.getId();

        // Lấy phần trăm thay đổi
        SummaryQueryService.FinancialChange change = summaryQueryService.getFinancialChangeForPeriod(userId, period);

        // Tạo header tùy chỉnh
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Assets-Change", String.valueOf(change.getAssetsChangePercentage()));
        headers.add("X-Income-Change", String.valueOf(change.getIncomeChangePercentage()));
        headers.add("X-Expense-Change", String.valueOf(change.getExpenseChangePercentage()));
        headers.add("X-Profit-Change", String.valueOf(change.getProfitChangePercentage()));

        return ResponseEntity.ok().headers(headers).body(change);
    }

    /**
     * {@code GET /summaries/detailed} : Lấy dữ liệu chi tiết tài chính cho biểu đồ.
     *
     * @param period Loại kỳ (WEEK, MONTH, YEAR)
     * @return Dữ liệu chi tiết tài chính (labels, incomeData, expenseData,
     *         progressRateData)
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedFinancialData(
            @RequestParam(value = "period", defaultValue = "MONTH") String period,
            @RequestHeader("Authorization") String authorization) {
        LOG.debug("REST request to get detailed financial data for period: {}", period);

        // Lấy người dùng hiện tại và xử lý nếu không tồn tại
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        Long userId = currentUser.getId();

        try {
            Map<String, Object> detailedData = summaryQueryService.getDetailedFinancialData(userId,
                    period.toUpperCase());

            LOG.debug("Detailed data for userId: {}, period: {}: {}", userId, period, detailedData);

            // Tạo header tùy chỉnh
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Labels-Count", String.valueOf(((List<?>) detailedData.get("labels")).size()));
            headers.add("X-Income-Data-Count", String.valueOf(((List<?>) detailedData.get("incomeData")).size()));
            headers.add("X-Expense-Data-Count", String.valueOf(((List<?>) detailedData.get("expenseData")).size()));

            return ResponseEntity.ok().headers(headers).body(detailedData);
        } catch (Exception e) {
            LOG.error("Error fetching detailed financial data for userId: {}, period: {}", userId, period, e);
            throw new BadRequestAlertException("Error fetching detailed data", ENTITY_NAME, "fetcherror");
        }
    }

    /**
     * Tính periodValue dựa trên ngày và loại kỳ.
     */
    private String getPeriodValue(LocalDate date, String periodType) {
        switch (periodType.toUpperCase()) {
            case "WEEK":
                int weekOfYear = date.get(WeekFields.ISO.weekOfWeekBasedYear());
                return date.getYear() + "-" + String.format("%02d", weekOfYear);
            case "MONTH":
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "YEAR":
                return String.valueOf(date.getYear());
            default:
                throw new IllegalArgumentException("Invalid period type: " + periodType);
        }
    }
}
