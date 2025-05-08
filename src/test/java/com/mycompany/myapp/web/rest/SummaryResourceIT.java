package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.SummaryAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.mycompany.myapp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.PeriodType;
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.SummaryService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SummaryResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SummaryResourceIT {

    private static final PeriodType DEFAULT_PERIOD_TYPE = PeriodType.WEEK;
    private static final PeriodType UPDATED_PERIOD_TYPE = PeriodType.MONTH;

    private static final String DEFAULT_PERIOD_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_PERIOD_VALUE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TOTAL_ASSETS = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_ASSETS = new BigDecimal(2);
    private static final BigDecimal SMALLER_TOTAL_ASSETS = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_TOTAL_INCOME = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_INCOME = new BigDecimal(2);
    private static final BigDecimal SMALLER_TOTAL_INCOME = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_TOTAL_EXPENSE = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_EXPENSE = new BigDecimal(2);
    private static final BigDecimal SMALLER_TOTAL_EXPENSE = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_TOTAL_PROFIT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_PROFIT = new BigDecimal(2);
    private static final BigDecimal SMALLER_TOTAL_PROFIT = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_PROFIT_PERCENTAGE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PROFIT_PERCENTAGE = new BigDecimal(2);
    private static final BigDecimal SMALLER_PROFIT_PERCENTAGE = new BigDecimal(1 - 1);

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/summaries";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private SummaryRepository summaryRepositoryMock;

    @Mock
    private SummaryService summaryServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSummaryMockMvc;

    private Summary summary;

    private Summary insertedSummary;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Summary createEntity(EntityManager em) {
        Summary summary = new Summary()
            .periodType(DEFAULT_PERIOD_TYPE)
            .periodValue(DEFAULT_PERIOD_VALUE)
            .totalAssets(DEFAULT_TOTAL_ASSETS)
            .totalIncome(DEFAULT_TOTAL_INCOME)
            .totalExpense(DEFAULT_TOTAL_EXPENSE)
            .totalProfit(DEFAULT_TOTAL_PROFIT)
            .profitPercentage(DEFAULT_PROFIT_PERCENTAGE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        summary.setUser(user);
        return summary;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Summary createUpdatedEntity(EntityManager em) {
        Summary updatedSummary = new Summary()
            .periodType(UPDATED_PERIOD_TYPE)
            .periodValue(UPDATED_PERIOD_VALUE)
            .totalAssets(UPDATED_TOTAL_ASSETS)
            .totalIncome(UPDATED_TOTAL_INCOME)
            .totalExpense(UPDATED_TOTAL_EXPENSE)
            .totalProfit(UPDATED_TOTAL_PROFIT)
            .profitPercentage(UPDATED_PROFIT_PERCENTAGE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedSummary.setUser(user);
        return updatedSummary;
    }

    @BeforeEach
    public void initTest() {
        summary = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedSummary != null) {
            summaryRepository.delete(insertedSummary);
            insertedSummary = null;
        }
    }

    @Test
    @Transactional
    void createSummary() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Summary
        var returnedSummary = om.readValue(
            restSummaryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(summary)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Summary.class
        );

        // Validate the Summary in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSummaryUpdatableFieldsEquals(returnedSummary, getPersistedSummary(returnedSummary));

        insertedSummary = returnedSummary;
    }

    @Test
    @Transactional
    void createSummaryWithExistingId() throws Exception {
        // Create the Summary with an existing ID
        summary.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSummaryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(summary)))
            .andExpect(status().isBadRequest());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPeriodTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        summary.setPeriodType(null);

        // Create the Summary, which fails.

        restSummaryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(summary)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPeriodValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        summary.setPeriodValue(null);

        // Create the Summary, which fails.

        restSummaryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(summary)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSummaries() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList
        restSummaryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(summary.getId().intValue())))
            .andExpect(jsonPath("$.[*].periodType").value(hasItem(DEFAULT_PERIOD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].periodValue").value(hasItem(DEFAULT_PERIOD_VALUE)))
            .andExpect(jsonPath("$.[*].totalAssets").value(hasItem(sameNumber(DEFAULT_TOTAL_ASSETS))))
            .andExpect(jsonPath("$.[*].totalIncome").value(hasItem(sameNumber(DEFAULT_TOTAL_INCOME))))
            .andExpect(jsonPath("$.[*].totalExpense").value(hasItem(sameNumber(DEFAULT_TOTAL_EXPENSE))))
            .andExpect(jsonPath("$.[*].totalProfit").value(hasItem(sameNumber(DEFAULT_TOTAL_PROFIT))))
            .andExpect(jsonPath("$.[*].profitPercentage").value(hasItem(sameNumber(DEFAULT_PROFIT_PERCENTAGE))))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSummariesWithEagerRelationshipsIsEnabled() throws Exception {
        when(summaryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSummaryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(summaryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSummariesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(summaryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSummaryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(summaryRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSummary() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get the summary
        restSummaryMockMvc
            .perform(get(ENTITY_API_URL_ID, summary.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(summary.getId().intValue()))
            .andExpect(jsonPath("$.periodType").value(DEFAULT_PERIOD_TYPE.toString()))
            .andExpect(jsonPath("$.periodValue").value(DEFAULT_PERIOD_VALUE))
            .andExpect(jsonPath("$.totalAssets").value(sameNumber(DEFAULT_TOTAL_ASSETS)))
            .andExpect(jsonPath("$.totalIncome").value(sameNumber(DEFAULT_TOTAL_INCOME)))
            .andExpect(jsonPath("$.totalExpense").value(sameNumber(DEFAULT_TOTAL_EXPENSE)))
            .andExpect(jsonPath("$.totalProfit").value(sameNumber(DEFAULT_TOTAL_PROFIT)))
            .andExpect(jsonPath("$.profitPercentage").value(sameNumber(DEFAULT_PROFIT_PERCENTAGE)))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getSummariesByIdFiltering() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        Long id = summary.getId();

        defaultSummaryFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSummaryFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSummaryFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodType equals to
        defaultSummaryFiltering("periodType.equals=" + DEFAULT_PERIOD_TYPE, "periodType.equals=" + UPDATED_PERIOD_TYPE);
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodType in
        defaultSummaryFiltering("periodType.in=" + DEFAULT_PERIOD_TYPE + "," + UPDATED_PERIOD_TYPE, "periodType.in=" + UPDATED_PERIOD_TYPE);
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodType is not null
        defaultSummaryFiltering("periodType.specified=true", "periodType.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodValueIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodValue equals to
        defaultSummaryFiltering("periodValue.equals=" + DEFAULT_PERIOD_VALUE, "periodValue.equals=" + UPDATED_PERIOD_VALUE);
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodValueIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodValue in
        defaultSummaryFiltering(
            "periodValue.in=" + DEFAULT_PERIOD_VALUE + "," + UPDATED_PERIOD_VALUE,
            "periodValue.in=" + UPDATED_PERIOD_VALUE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodValue is not null
        defaultSummaryFiltering("periodValue.specified=true", "periodValue.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodValueContainsSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodValue contains
        defaultSummaryFiltering("periodValue.contains=" + DEFAULT_PERIOD_VALUE, "periodValue.contains=" + UPDATED_PERIOD_VALUE);
    }

    @Test
    @Transactional
    void getAllSummariesByPeriodValueNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where periodValue does not contain
        defaultSummaryFiltering("periodValue.doesNotContain=" + UPDATED_PERIOD_VALUE, "periodValue.doesNotContain=" + DEFAULT_PERIOD_VALUE);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets equals to
        defaultSummaryFiltering("totalAssets.equals=" + DEFAULT_TOTAL_ASSETS, "totalAssets.equals=" + UPDATED_TOTAL_ASSETS);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets in
        defaultSummaryFiltering(
            "totalAssets.in=" + DEFAULT_TOTAL_ASSETS + "," + UPDATED_TOTAL_ASSETS,
            "totalAssets.in=" + UPDATED_TOTAL_ASSETS
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets is not null
        defaultSummaryFiltering("totalAssets.specified=true", "totalAssets.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets is greater than or equal to
        defaultSummaryFiltering(
            "totalAssets.greaterThanOrEqual=" + DEFAULT_TOTAL_ASSETS,
            "totalAssets.greaterThanOrEqual=" + UPDATED_TOTAL_ASSETS
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets is less than or equal to
        defaultSummaryFiltering(
            "totalAssets.lessThanOrEqual=" + DEFAULT_TOTAL_ASSETS,
            "totalAssets.lessThanOrEqual=" + SMALLER_TOTAL_ASSETS
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets is less than
        defaultSummaryFiltering("totalAssets.lessThan=" + UPDATED_TOTAL_ASSETS, "totalAssets.lessThan=" + DEFAULT_TOTAL_ASSETS);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalAssetsIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalAssets is greater than
        defaultSummaryFiltering("totalAssets.greaterThan=" + SMALLER_TOTAL_ASSETS, "totalAssets.greaterThan=" + DEFAULT_TOTAL_ASSETS);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome equals to
        defaultSummaryFiltering("totalIncome.equals=" + DEFAULT_TOTAL_INCOME, "totalIncome.equals=" + UPDATED_TOTAL_INCOME);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome in
        defaultSummaryFiltering(
            "totalIncome.in=" + DEFAULT_TOTAL_INCOME + "," + UPDATED_TOTAL_INCOME,
            "totalIncome.in=" + UPDATED_TOTAL_INCOME
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome is not null
        defaultSummaryFiltering("totalIncome.specified=true", "totalIncome.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome is greater than or equal to
        defaultSummaryFiltering(
            "totalIncome.greaterThanOrEqual=" + DEFAULT_TOTAL_INCOME,
            "totalIncome.greaterThanOrEqual=" + UPDATED_TOTAL_INCOME
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome is less than or equal to
        defaultSummaryFiltering(
            "totalIncome.lessThanOrEqual=" + DEFAULT_TOTAL_INCOME,
            "totalIncome.lessThanOrEqual=" + SMALLER_TOTAL_INCOME
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome is less than
        defaultSummaryFiltering("totalIncome.lessThan=" + UPDATED_TOTAL_INCOME, "totalIncome.lessThan=" + DEFAULT_TOTAL_INCOME);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalIncomeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalIncome is greater than
        defaultSummaryFiltering("totalIncome.greaterThan=" + SMALLER_TOTAL_INCOME, "totalIncome.greaterThan=" + DEFAULT_TOTAL_INCOME);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense equals to
        defaultSummaryFiltering("totalExpense.equals=" + DEFAULT_TOTAL_EXPENSE, "totalExpense.equals=" + UPDATED_TOTAL_EXPENSE);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense in
        defaultSummaryFiltering(
            "totalExpense.in=" + DEFAULT_TOTAL_EXPENSE + "," + UPDATED_TOTAL_EXPENSE,
            "totalExpense.in=" + UPDATED_TOTAL_EXPENSE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense is not null
        defaultSummaryFiltering("totalExpense.specified=true", "totalExpense.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense is greater than or equal to
        defaultSummaryFiltering(
            "totalExpense.greaterThanOrEqual=" + DEFAULT_TOTAL_EXPENSE,
            "totalExpense.greaterThanOrEqual=" + UPDATED_TOTAL_EXPENSE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense is less than or equal to
        defaultSummaryFiltering(
            "totalExpense.lessThanOrEqual=" + DEFAULT_TOTAL_EXPENSE,
            "totalExpense.lessThanOrEqual=" + SMALLER_TOTAL_EXPENSE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense is less than
        defaultSummaryFiltering("totalExpense.lessThan=" + UPDATED_TOTAL_EXPENSE, "totalExpense.lessThan=" + DEFAULT_TOTAL_EXPENSE);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalExpenseIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalExpense is greater than
        defaultSummaryFiltering("totalExpense.greaterThan=" + SMALLER_TOTAL_EXPENSE, "totalExpense.greaterThan=" + DEFAULT_TOTAL_EXPENSE);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit equals to
        defaultSummaryFiltering("totalProfit.equals=" + DEFAULT_TOTAL_PROFIT, "totalProfit.equals=" + UPDATED_TOTAL_PROFIT);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit in
        defaultSummaryFiltering(
            "totalProfit.in=" + DEFAULT_TOTAL_PROFIT + "," + UPDATED_TOTAL_PROFIT,
            "totalProfit.in=" + UPDATED_TOTAL_PROFIT
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit is not null
        defaultSummaryFiltering("totalProfit.specified=true", "totalProfit.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit is greater than or equal to
        defaultSummaryFiltering(
            "totalProfit.greaterThanOrEqual=" + DEFAULT_TOTAL_PROFIT,
            "totalProfit.greaterThanOrEqual=" + UPDATED_TOTAL_PROFIT
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit is less than or equal to
        defaultSummaryFiltering(
            "totalProfit.lessThanOrEqual=" + DEFAULT_TOTAL_PROFIT,
            "totalProfit.lessThanOrEqual=" + SMALLER_TOTAL_PROFIT
        );
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit is less than
        defaultSummaryFiltering("totalProfit.lessThan=" + UPDATED_TOTAL_PROFIT, "totalProfit.lessThan=" + DEFAULT_TOTAL_PROFIT);
    }

    @Test
    @Transactional
    void getAllSummariesByTotalProfitIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where totalProfit is greater than
        defaultSummaryFiltering("totalProfit.greaterThan=" + SMALLER_TOTAL_PROFIT, "totalProfit.greaterThan=" + DEFAULT_TOTAL_PROFIT);
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage equals to
        defaultSummaryFiltering(
            "profitPercentage.equals=" + DEFAULT_PROFIT_PERCENTAGE,
            "profitPercentage.equals=" + UPDATED_PROFIT_PERCENTAGE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage in
        defaultSummaryFiltering(
            "profitPercentage.in=" + DEFAULT_PROFIT_PERCENTAGE + "," + UPDATED_PROFIT_PERCENTAGE,
            "profitPercentage.in=" + UPDATED_PROFIT_PERCENTAGE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage is not null
        defaultSummaryFiltering("profitPercentage.specified=true", "profitPercentage.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage is greater than or equal to
        defaultSummaryFiltering(
            "profitPercentage.greaterThanOrEqual=" + DEFAULT_PROFIT_PERCENTAGE,
            "profitPercentage.greaterThanOrEqual=" + UPDATED_PROFIT_PERCENTAGE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage is less than or equal to
        defaultSummaryFiltering(
            "profitPercentage.lessThanOrEqual=" + DEFAULT_PROFIT_PERCENTAGE,
            "profitPercentage.lessThanOrEqual=" + SMALLER_PROFIT_PERCENTAGE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage is less than
        defaultSummaryFiltering(
            "profitPercentage.lessThan=" + UPDATED_PROFIT_PERCENTAGE,
            "profitPercentage.lessThan=" + DEFAULT_PROFIT_PERCENTAGE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByProfitPercentageIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where profitPercentage is greater than
        defaultSummaryFiltering(
            "profitPercentage.greaterThan=" + SMALLER_PROFIT_PERCENTAGE,
            "profitPercentage.greaterThan=" + DEFAULT_PROFIT_PERCENTAGE
        );
    }

    @Test
    @Transactional
    void getAllSummariesByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where createdAt equals to
        defaultSummaryFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllSummariesByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where createdAt in
        defaultSummaryFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllSummariesByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where createdAt is not null
        defaultSummaryFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where updatedAt equals to
        defaultSummaryFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllSummariesByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where updatedAt in
        defaultSummaryFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllSummariesByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        // Get all the summaryList where updatedAt is not null
        defaultSummaryFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllSummariesByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            summaryRepository.saveAndFlush(summary);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        summary.setUser(user);
        summaryRepository.saveAndFlush(summary);
        Long userId = user.getId();
        // Get all the summaryList where user equals to userId
        defaultSummaryShouldBeFound("userId.equals=" + userId);

        // Get all the summaryList where user equals to (userId + 1)
        defaultSummaryShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultSummaryFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSummaryShouldBeFound(shouldBeFound);
        defaultSummaryShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSummaryShouldBeFound(String filter) throws Exception {
        restSummaryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(summary.getId().intValue())))
            .andExpect(jsonPath("$.[*].periodType").value(hasItem(DEFAULT_PERIOD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].periodValue").value(hasItem(DEFAULT_PERIOD_VALUE)))
            .andExpect(jsonPath("$.[*].totalAssets").value(hasItem(sameNumber(DEFAULT_TOTAL_ASSETS))))
            .andExpect(jsonPath("$.[*].totalIncome").value(hasItem(sameNumber(DEFAULT_TOTAL_INCOME))))
            .andExpect(jsonPath("$.[*].totalExpense").value(hasItem(sameNumber(DEFAULT_TOTAL_EXPENSE))))
            .andExpect(jsonPath("$.[*].totalProfit").value(hasItem(sameNumber(DEFAULT_TOTAL_PROFIT))))
            .andExpect(jsonPath("$.[*].profitPercentage").value(hasItem(sameNumber(DEFAULT_PROFIT_PERCENTAGE))))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restSummaryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSummaryShouldNotBeFound(String filter) throws Exception {
        restSummaryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSummaryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSummary() throws Exception {
        // Get the summary
        restSummaryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSummary() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the summary
        Summary updatedSummary = summaryRepository.findById(summary.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSummary are not directly saved in db
        em.detach(updatedSummary);
        updatedSummary
            .periodType(UPDATED_PERIOD_TYPE)
            .periodValue(UPDATED_PERIOD_VALUE)
            .totalAssets(UPDATED_TOTAL_ASSETS)
            .totalIncome(UPDATED_TOTAL_INCOME)
            .totalExpense(UPDATED_TOTAL_EXPENSE)
            .totalProfit(UPDATED_TOTAL_PROFIT)
            .profitPercentage(UPDATED_PROFIT_PERCENTAGE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restSummaryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSummary.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSummary))
            )
            .andExpect(status().isOk());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSummaryToMatchAllProperties(updatedSummary);
    }

    @Test
    @Transactional
    void putNonExistingSummary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        summary.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSummaryMockMvc
            .perform(put(ENTITY_API_URL_ID, summary.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(summary)))
            .andExpect(status().isBadRequest());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSummary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        summary.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSummaryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(summary))
            )
            .andExpect(status().isBadRequest());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSummary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        summary.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSummaryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(summary)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSummaryWithPatch() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the summary using partial update
        Summary partialUpdatedSummary = new Summary();
        partialUpdatedSummary.setId(summary.getId());

        partialUpdatedSummary
            .totalIncome(UPDATED_TOTAL_INCOME)
            .totalExpense(UPDATED_TOTAL_EXPENSE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSummary.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSummary))
            )
            .andExpect(status().isOk());

        // Validate the Summary in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSummaryUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSummary, summary), getPersistedSummary(summary));
    }

    @Test
    @Transactional
    void fullUpdateSummaryWithPatch() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the summary using partial update
        Summary partialUpdatedSummary = new Summary();
        partialUpdatedSummary.setId(summary.getId());

        partialUpdatedSummary
            .periodType(UPDATED_PERIOD_TYPE)
            .periodValue(UPDATED_PERIOD_VALUE)
            .totalAssets(UPDATED_TOTAL_ASSETS)
            .totalIncome(UPDATED_TOTAL_INCOME)
            .totalExpense(UPDATED_TOTAL_EXPENSE)
            .totalProfit(UPDATED_TOTAL_PROFIT)
            .profitPercentage(UPDATED_PROFIT_PERCENTAGE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSummary.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSummary))
            )
            .andExpect(status().isOk());

        // Validate the Summary in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSummaryUpdatableFieldsEquals(partialUpdatedSummary, getPersistedSummary(partialUpdatedSummary));
    }

    @Test
    @Transactional
    void patchNonExistingSummary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        summary.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, summary.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(summary))
            )
            .andExpect(status().isBadRequest());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSummary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        summary.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSummaryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(summary))
            )
            .andExpect(status().isBadRequest());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSummary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        summary.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSummaryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(summary)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Summary in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSummary() throws Exception {
        // Initialize the database
        insertedSummary = summaryRepository.saveAndFlush(summary);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the summary
        restSummaryMockMvc
            .perform(delete(ENTITY_API_URL_ID, summary.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return summaryRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Summary getPersistedSummary(Summary summary) {
        return summaryRepository.findById(summary.getId()).orElseThrow();
    }

    protected void assertPersistedSummaryToMatchAllProperties(Summary expectedSummary) {
        assertSummaryAllPropertiesEquals(expectedSummary, getPersistedSummary(expectedSummary));
    }

    protected void assertPersistedSummaryToMatchUpdatableProperties(Summary expectedSummary) {
        assertSummaryAllUpdatablePropertiesEquals(expectedSummary, getPersistedSummary(expectedSummary));
    }
}
