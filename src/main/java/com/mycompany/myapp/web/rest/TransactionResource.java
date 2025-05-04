package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.service.SummaryQueryService;
import com.mycompany.myapp.service.NotificationQueryService;
import com.mycompany.myapp.service.TransactionQueryService;
import com.mycompany.myapp.service.TransactionService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.service.criteria.TransactionCriteria;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import tech.jhipster.service.filter.LongFilter;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Transaction}.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionResource {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionResource.class);

    private static final String ENTITY_NAME = "transaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransactionService transactionService;

    private final TransactionRepository transactionRepository;

    private final TransactionQueryService transactionQueryService;
    private final SummaryQueryService summaryQueryService;
    private final NotificationQueryService notificationQueryService;
    private final UserService userService;

    public TransactionResource(
            TransactionService transactionService,
            TransactionRepository transactionRepository,
            TransactionQueryService transactionQueryService,
            SummaryQueryService summaryQueryService,
            UserService userService, 
            NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.transactionQueryService = transactionQueryService;
        this.summaryQueryService = summaryQueryService;
        this.userService = userService;
    }

    @PostMapping("")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction)
            throws URISyntaxException {
        LOG.debug("REST request to save Transaction : {}", transaction);
        if (transaction.getId() != null) {
            throw new BadRequestAlertException("A new transaction cannot already have an ID", ENTITY_NAME, "idexists");
        }

        // Gán người dùng hiện tại cho giao dịch
        Optional<User> currentUser = userService.getUserWithAuthorities();
        if (!currentUser.isPresent()) {
            LOG.warn("Current user not found");
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        // Lưu giao dịch
        Transaction savedTransaction = transactionService.save(transaction);

        // Cập nhật Summary
        summaryQueryService.updateSummaryForTransaction(currentUser.get().getId(), null, savedTransaction);

        // Call NotificationService to check and create notifications
        if(savedTransaction.getCategory() != null) {
            notificationQueryService.createNotificationForTransaction(currentUser.get().getId(), savedTransaction);
            notificationQueryService.createWarningNotificationForTransaction(currentUser.get().getId(), savedTransaction);
        }

        return ResponseEntity.created(new URI("/api/transactions/" + savedTransaction.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, savedTransaction.getId().toString()))
            .body(savedTransaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody Transaction transaction) throws URISyntaxException {
        LOG.debug("REST request to update Transaction : {}, {}", id, transaction);
        if (transaction.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transaction.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!transactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Kiểm tra quyền truy cập
        Optional<User> currentUser = userService.getUserWithAuthorities();
        if (!currentUser.isPresent()) {
            LOG.warn("Current user not found");
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        Transaction existingTransaction = transactionRepository.findById(id).orElseThrow();
        if (!existingTransaction.getUser().getId().equals(currentUser.get().getId())) {
            LOG.warn("User {} attempted to update transaction {} that does not belong to them",
                    currentUser.get().getId(), id);
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Lưu giao dịch cũ để cập nhật Summary
        Transaction oldTransaction = transactionRepository.findById(id).orElseThrow();

        // Cập nhật giao dịch
        Transaction updatedTransaction = transactionService.update(transaction);

        // Cập nhật Summary: trừ giá trị cũ và cộng giá trị mới trong một lần gọi
        summaryQueryService.updateSummaryForTransaction(currentUser.get().getId(), oldTransaction, updatedTransaction);

        notificationQueryService.createNotificationForTransaction(currentUser.get().getId(), updatedTransaction);


        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        transaction.getId().toString()))
                .body(updatedTransaction);
    }

    /**
     * {@code PATCH  /transactions/:id} : Partial updates given fields of an
     * existing transaction, field will ignore if it is null
     *
     * @param id          the id of the transaction to save.
     * @param transaction the transaction to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated transaction,
     *         or with status {@code 400 (Bad Request)} if the transaction is not
     *         valid,
     *         or with status {@code 404 (Not Found)} if the transaction is not
     *         found,
     *         or with status {@code 500 (Internal Server Error)} if the transaction
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Transaction> partialUpdateTransaction(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody Transaction transaction) throws URISyntaxException {
        LOG.debug("REST request to partial update Transaction partially : {}, {}", id, transaction);
        if (transaction.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, transaction.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!transactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Transaction> result = transactionService.partialUpdate(transaction);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, transaction.getId().toString()));
    }

    /**
     * {@code GET  /transactions} : get all the transactions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of transactions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Transaction>> getAllTransactions(
            TransactionCriteria criteria,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get Transactions by criteria: {}", criteria);

        // Lấy người dùng hiện tại
        Optional<User> currentUser = userService.getUserWithAuthorities();
        if (!currentUser.isPresent()) {
            LOG.warn("Current user not found");
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        // Gán userId của người dùng hiện tại vào criteria
        LongFilter userIdFilter = new LongFilter();
        userIdFilter.setEquals(currentUser.get().getId());
        criteria.setUserId(userIdFilter);

        // Tìm kiếm giao dịch dựa trên criteria và phân trang
        Page<Transaction> page = transactionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /transactions/count} : count all the transactions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTransactions(TransactionCriteria criteria) {
        LOG.debug("REST request to count Transactions by criteria: {}", criteria);
        return ResponseEntity.ok().body(transactionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /transactions/:id} : get the "id" transaction.
     *
     * @param id the id of the transaction to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the transaction, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Transaction : {}", id);
        Optional<Transaction> transaction = transactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(transaction);
    }

    /**
     * {@code DELETE  /transactions/:id} : delete the "id" transaction.
     *
     * @param id the id of the transaction to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Transaction : {}", id);

        // Lấy người dùng hiện tại
        Optional<User> currentUser = userService.getUserWithAuthorities();
        if (!currentUser.isPresent()) {
            LOG.warn("Current user not found");
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        // Kiểm tra quyền truy cập
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
        if (!transaction.getUser().getId().equals(currentUser.get().getId())) {
            LOG.warn("User {} attempted to delete transaction {} that does not belong to them",
                    currentUser.get().getId(), id);
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Cập nhật Summary trước khi xóa (trường hợp xóa: newTransaction = null)
        summaryQueryService.updateSummaryForTransaction(currentUser.get().getId(), transaction, null);

        // Xóa giao dịch
        transactionService.delete(id);

        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactions(
        @RequestParam(required = false) Long category,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) String type,
        @RequestHeader(value = "Accept-Language", defaultValue = "en") String language
    ) {
        LOG.debug("REST request to export Transactions with filters: category={}, fromDate={}, toDate={}, type={}", category, fromDate, toDate, type);

        // Convert date strings to LocalDate
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;

        // Fetch filtered transactions
        List<Transaction> transactions = transactionQueryService.findByFilters(category, from, to, type);

        // Generate Excel file with language support
        byte[] excelFile = transactionQueryService.exportToExcel(transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.xlsx");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok().headers(headers).body(excelFile);
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportTransactionsToPDF(
        @RequestParam(required = false) Long category,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) String type
    ) {
        LOG.debug("REST request to export Transactions to PDF with filters: category={}, fromDate={}, toDate={}, type={}", category, fromDate, toDate, type);

        // Convert date strings to LocalDate
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;

        // Fetch filtered transactions
        List<Transaction> transactions = transactionQueryService.findByFilters(category, from, to, type);

        // Generate PDF file
        byte[] pdfFile = transactionQueryService.exportToPDF(transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfFile);
    }
}
