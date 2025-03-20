package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.BudgetAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.mycompany.myapp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Budget;
import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.BudgetRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.BudgetService;
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
 * Integration tests for the {@link BudgetResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class BudgetResourceIT {

    private static final BigDecimal DEFAULT_BUDGET_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_BUDGET_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_BUDGET_AMOUNT = new BigDecimal(1 - 1);

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/budgets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepositoryMock;

    @Mock
    private BudgetService budgetServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBudgetMockMvc;

    private Budget budget;

    private Budget insertedBudget;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Budget createEntity() {
        return new Budget()
            .budgetAmount(DEFAULT_BUDGET_AMOUNT)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Budget createUpdatedEntity() {
        return new Budget()
            .budgetAmount(UPDATED_BUDGET_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    public void initTest() {
        budget = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedBudget != null) {
            budgetRepository.delete(insertedBudget);
            insertedBudget = null;
        }
    }

    @Test
    @Transactional
    void createBudget() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Budget
        var returnedBudget = om.readValue(
            restBudgetMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Budget.class
        );

        // Validate the Budget in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertBudgetUpdatableFieldsEquals(returnedBudget, getPersistedBudget(returnedBudget));

        insertedBudget = returnedBudget;
    }

    @Test
    @Transactional
    void createBudgetWithExistingId() throws Exception {
        // Create the Budget with an existing ID
        budget.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBudgetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
            .andExpect(status().isBadRequest());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkBudgetAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        budget.setBudgetAmount(null);

        // Create the Budget, which fails.

        restBudgetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        budget.setStartDate(null);

        // Create the Budget, which fails.

        restBudgetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        budget.setEndDate(null);

        // Create the Budget, which fails.

        restBudgetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBudgets() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList
        restBudgetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(budget.getId().intValue())))
            .andExpect(jsonPath("$.[*].budgetAmount").value(hasItem(sameNumber(DEFAULT_BUDGET_AMOUNT))))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBudgetsWithEagerRelationshipsIsEnabled() throws Exception {
        when(budgetServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBudgetMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(budgetServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBudgetsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(budgetServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBudgetMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(budgetRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getBudget() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get the budget
        restBudgetMockMvc
            .perform(get(ENTITY_API_URL_ID, budget.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(budget.getId().intValue()))
            .andExpect(jsonPath("$.budgetAmount").value(sameNumber(DEFAULT_BUDGET_AMOUNT)))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getBudgetsByIdFiltering() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        Long id = budget.getId();

        defaultBudgetFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultBudgetFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultBudgetFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount equals to
        defaultBudgetFiltering("budgetAmount.equals=" + DEFAULT_BUDGET_AMOUNT, "budgetAmount.equals=" + UPDATED_BUDGET_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount in
        defaultBudgetFiltering(
            "budgetAmount.in=" + DEFAULT_BUDGET_AMOUNT + "," + UPDATED_BUDGET_AMOUNT,
            "budgetAmount.in=" + UPDATED_BUDGET_AMOUNT
        );
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount is not null
        defaultBudgetFiltering("budgetAmount.specified=true", "budgetAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount is greater than or equal to
        defaultBudgetFiltering(
            "budgetAmount.greaterThanOrEqual=" + DEFAULT_BUDGET_AMOUNT,
            "budgetAmount.greaterThanOrEqual=" + UPDATED_BUDGET_AMOUNT
        );
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount is less than or equal to
        defaultBudgetFiltering(
            "budgetAmount.lessThanOrEqual=" + DEFAULT_BUDGET_AMOUNT,
            "budgetAmount.lessThanOrEqual=" + SMALLER_BUDGET_AMOUNT
        );
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount is less than
        defaultBudgetFiltering("budgetAmount.lessThan=" + UPDATED_BUDGET_AMOUNT, "budgetAmount.lessThan=" + DEFAULT_BUDGET_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBudgetsByBudgetAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where budgetAmount is greater than
        defaultBudgetFiltering("budgetAmount.greaterThan=" + SMALLER_BUDGET_AMOUNT, "budgetAmount.greaterThan=" + DEFAULT_BUDGET_AMOUNT);
    }

    @Test
    @Transactional
    void getAllBudgetsByStartDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where startDate equals to
        defaultBudgetFiltering("startDate.equals=" + DEFAULT_START_DATE, "startDate.equals=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    void getAllBudgetsByStartDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where startDate in
        defaultBudgetFiltering("startDate.in=" + DEFAULT_START_DATE + "," + UPDATED_START_DATE, "startDate.in=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    void getAllBudgetsByStartDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where startDate is not null
        defaultBudgetFiltering("startDate.specified=true", "startDate.specified=false");
    }

    @Test
    @Transactional
    void getAllBudgetsByEndDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where endDate equals to
        defaultBudgetFiltering("endDate.equals=" + DEFAULT_END_DATE, "endDate.equals=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    void getAllBudgetsByEndDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where endDate in
        defaultBudgetFiltering("endDate.in=" + DEFAULT_END_DATE + "," + UPDATED_END_DATE, "endDate.in=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    void getAllBudgetsByEndDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where endDate is not null
        defaultBudgetFiltering("endDate.specified=true", "endDate.specified=false");
    }

    @Test
    @Transactional
    void getAllBudgetsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where createdAt equals to
        defaultBudgetFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllBudgetsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where createdAt in
        defaultBudgetFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllBudgetsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where createdAt is not null
        defaultBudgetFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllBudgetsByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where updatedAt equals to
        defaultBudgetFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllBudgetsByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where updatedAt in
        defaultBudgetFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllBudgetsByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        // Get all the budgetList where updatedAt is not null
        defaultBudgetFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllBudgetsByCategoryIsEqualToSomething() throws Exception {
        Category category;
        if (TestUtil.findAll(em, Category.class).isEmpty()) {
            budgetRepository.saveAndFlush(budget);
            category = CategoryResourceIT.createEntity();
        } else {
            category = TestUtil.findAll(em, Category.class).get(0);
        }
        em.persist(category);
        em.flush();
        budget.setCategory(category);
        budgetRepository.saveAndFlush(budget);
        Long categoryId = category.getId();
        // Get all the budgetList where category equals to categoryId
        defaultBudgetShouldBeFound("categoryId.equals=" + categoryId);

        // Get all the budgetList where category equals to (categoryId + 1)
        defaultBudgetShouldNotBeFound("categoryId.equals=" + (categoryId + 1));
    }

    @Test
    @Transactional
    void getAllBudgetsByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            budgetRepository.saveAndFlush(budget);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        budget.setUser(user);
        budgetRepository.saveAndFlush(budget);
        Long userId = user.getId();
        // Get all the budgetList where user equals to userId
        defaultBudgetShouldBeFound("userId.equals=" + userId);

        // Get all the budgetList where user equals to (userId + 1)
        defaultBudgetShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultBudgetFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultBudgetShouldBeFound(shouldBeFound);
        defaultBudgetShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBudgetShouldBeFound(String filter) throws Exception {
        restBudgetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(budget.getId().intValue())))
            .andExpect(jsonPath("$.[*].budgetAmount").value(hasItem(sameNumber(DEFAULT_BUDGET_AMOUNT))))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restBudgetMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBudgetShouldNotBeFound(String filter) throws Exception {
        restBudgetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBudgetMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBudget() throws Exception {
        // Get the budget
        restBudgetMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBudget() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the budget
        Budget updatedBudget = budgetRepository.findById(budget.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBudget are not directly saved in db
        em.detach(updatedBudget);
        updatedBudget
            .budgetAmount(UPDATED_BUDGET_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restBudgetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedBudget.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedBudget))
            )
            .andExpect(status().isOk());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBudgetToMatchAllProperties(updatedBudget);
    }

    @Test
    @Transactional
    void putNonExistingBudget() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        budget.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBudgetMockMvc
            .perform(put(ENTITY_API_URL_ID, budget.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
            .andExpect(status().isBadRequest());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBudget() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        budget.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBudgetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(budget))
            )
            .andExpect(status().isBadRequest());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBudget() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        budget.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBudgetMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(budget)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBudgetWithPatch() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the budget using partial update
        Budget partialUpdatedBudget = new Budget();
        partialUpdatedBudget.setId(budget.getId());

        partialUpdatedBudget.budgetAmount(UPDATED_BUDGET_AMOUNT).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);

        restBudgetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBudget.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBudget))
            )
            .andExpect(status().isOk());

        // Validate the Budget in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBudgetUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBudget, budget), getPersistedBudget(budget));
    }

    @Test
    @Transactional
    void fullUpdateBudgetWithPatch() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the budget using partial update
        Budget partialUpdatedBudget = new Budget();
        partialUpdatedBudget.setId(budget.getId());

        partialUpdatedBudget
            .budgetAmount(UPDATED_BUDGET_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restBudgetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBudget.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBudget))
            )
            .andExpect(status().isOk());

        // Validate the Budget in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBudgetUpdatableFieldsEquals(partialUpdatedBudget, getPersistedBudget(partialUpdatedBudget));
    }

    @Test
    @Transactional
    void patchNonExistingBudget() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        budget.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBudgetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, budget.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(budget))
            )
            .andExpect(status().isBadRequest());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBudget() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        budget.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBudgetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(budget))
            )
            .andExpect(status().isBadRequest());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBudget() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        budget.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBudgetMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(budget)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Budget in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBudget() throws Exception {
        // Initialize the database
        insertedBudget = budgetRepository.saveAndFlush(budget);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the budget
        restBudgetMockMvc
            .perform(delete(ENTITY_API_URL_ID, budget.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return budgetRepository.count();
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

    protected Budget getPersistedBudget(Budget budget) {
        return budgetRepository.findById(budget.getId()).orElseThrow();
    }

    protected void assertPersistedBudgetToMatchAllProperties(Budget expectedBudget) {
        assertBudgetAllPropertiesEquals(expectedBudget, getPersistedBudget(expectedBudget));
    }

    protected void assertPersistedBudgetToMatchUpdatableProperties(Budget expectedBudget) {
        assertBudgetAllUpdatablePropertiesEquals(expectedBudget, getPersistedBudget(expectedBudget));
    }
}
