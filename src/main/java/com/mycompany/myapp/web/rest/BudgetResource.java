package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Budget;
// import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.BudgetRepository;
import com.mycompany.myapp.service.BudgetQueryService;
import com.mycompany.myapp.service.BudgetService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.service.criteria.BudgetCriteria;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Budget}.
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetResource {

    private static final Logger LOG = LoggerFactory.getLogger(BudgetResource.class);

    private static final String ENTITY_NAME = "budget";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BudgetService budgetService;

    private final BudgetRepository budgetRepository;

    private final BudgetQueryService budgetQueryService;

    private final UserService userService;

    public BudgetResource(
            BudgetService budgetService,
            BudgetRepository budgetRepository,
            BudgetQueryService budgetQueryService,
            UserService userService) {
        this.userService = userService;
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
        this.budgetQueryService = budgetQueryService;
    }

    /**
     * {@code POST  /budgets} : Create a new budget.
     *
     * @param budget the budget to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new budget, or with status {@code 400 (Bad Request)} if the
     *         budget has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget) throws URISyntaxException {
        LOG.debug("REST request to save Budget : {}", budget);
        if (budget.getId() != null) {
            throw new BadRequestAlertException("A new budget cannot already have an ID", ENTITY_NAME, "idexists");
        }
        budget = budgetService.save(budget);
        return ResponseEntity.created(new URI("/api/budgets/" + budget.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        budget.getId().toString()))
                .body(budget);
    }

    /**
     * {@code PUT  /budgets/:id} : Updates an existing budget.
     *
     * @param id     the id of the budget to save.
     * @param budget the budget to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated budget,
     *         or with status {@code 400 (Bad Request)} if the budget is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the budget
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody Budget budget) throws URISyntaxException {
        LOG.debug("REST request to update Budget : {}, {}", id, budget);
        if (budget.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, budget.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!budgetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        budget = budgetService.update(budget);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        budget.getId().toString()))
                .body(budget);
    }

    /**
     * {@code PATCH  /budgets/:id} : Partial updates given fields of an existing
     * budget, field will ignore if it is null
     *
     * @param id     the id of the budget to save.
     * @param budget the budget to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated budget,
     *         or with status {@code 400 (Bad Request)} if the budget is not valid,
     *         or with status {@code 404 (Not Found)} if the budget is not found,
     *         or with status {@code 500 (Internal Server Error)} if the budget
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Budget> partialUpdateBudget(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody Budget budget) throws URISyntaxException {
        LOG.debug("REST request to partial update Budget partially : {}, {}", id, budget);
        if (budget.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, budget.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!budgetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Budget> result = budgetService.partialUpdate(budget);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, budget.getId().toString()));
    }

    /**
     * {@code GET  /budgets} : get all the budgets.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of budgets in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Budget>> getAllBudgets(
            BudgetCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get Budgets by criteria: {}", criteria);

        // Lấy người dùng hiện tại và xử lý nếu không tồn tại
        User currentUser = userService.getUserWithAuthorities()
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        // Gán userId của người dùng hiện tại vào criteria
        LongFilter userIdFilter = new LongFilter();
        userIdFilter.setEquals(currentUser.getId());
        criteria.setUserId(userIdFilter);

        Page<Budget> page = budgetQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /budgets/count} : count all the budgets.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countBudgets(BudgetCriteria criteria) {
        LOG.debug("REST request to count Budgets by criteria: {}", criteria);
        return ResponseEntity.ok().body(budgetQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /budgets/:id} : get the "id" budget.
     *
     * @param id the id of the budget to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the budget, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudget(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Budget : {}", id);
        Optional<Budget> budget = budgetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(budget);
    }

    /**
     * {@code DELETE  /budgets/:id} : delete the "id" budget.
     *
     * @param id the id of the budget to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Budget : {}", id);
        budgetService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }
}
