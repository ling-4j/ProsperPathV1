package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.CategoryAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.CategoryType;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.CategoryService;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link CategoryResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CategoryResourceIT {

    private static final String DEFAULT_CATEGORY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY_NAME = "BBBBBBBBBB";

    private static final CategoryType DEFAULT_CATEGORY_TYPE = CategoryType.INCOME;
    private static final CategoryType UPDATED_CATEGORY_TYPE = CategoryType.EXPENSE;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CATEGORY_ICON = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY_ICON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/categories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepositoryMock;

    @Mock
    private CategoryService categoryServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCategoryMockMvc;

    private Category category;

    private Category insertedCategory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Category createEntity() {
        return new Category()
            .categoryName(DEFAULT_CATEGORY_NAME)
            .categoryType(DEFAULT_CATEGORY_TYPE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .categoryIcon(DEFAULT_CATEGORY_ICON);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Category createUpdatedEntity() {
        return new Category()
            .categoryName(UPDATED_CATEGORY_NAME)
            .categoryType(UPDATED_CATEGORY_TYPE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .categoryIcon(UPDATED_CATEGORY_ICON);
    }

    @BeforeEach
    public void initTest() {
        category = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedCategory != null) {
            categoryRepository.delete(insertedCategory);
            insertedCategory = null;
        }
    }

    @Test
    @Transactional
    void createCategory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Category
        var returnedCategory = om.readValue(
            restCategoryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(category)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Category.class
        );

        // Validate the Category in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCategoryUpdatableFieldsEquals(returnedCategory, getPersistedCategory(returnedCategory));

        insertedCategory = returnedCategory;
    }

    @Test
    @Transactional
    void createCategoryWithExistingId() throws Exception {
        // Create the Category with an existing ID
        category.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(category)))
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCategoryNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        category.setCategoryName(null);

        // Create the Category, which fails.

        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(category)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCategoryTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        category.setCategoryType(null);

        // Create the Category, which fails.

        restCategoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(category)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCategories() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            .andExpect(jsonPath("$.[*].categoryName").value(hasItem(DEFAULT_CATEGORY_NAME)))
            .andExpect(jsonPath("$.[*].categoryType").value(hasItem(DEFAULT_CATEGORY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].categoryIcon").value(hasItem(DEFAULT_CATEGORY_ICON)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCategoriesWithEagerRelationshipsIsEnabled() throws Exception {
        when(categoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(categoryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCategoriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(categoryServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(categoryRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCategory() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get the category
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL_ID, category.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(category.getId().intValue()))
            .andExpect(jsonPath("$.categoryName").value(DEFAULT_CATEGORY_NAME))
            .andExpect(jsonPath("$.categoryType").value(DEFAULT_CATEGORY_TYPE.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            .andExpect(jsonPath("$.categoryIcon").value(DEFAULT_CATEGORY_ICON));
    }

    @Test
    @Transactional
    void getCategoriesByIdFiltering() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        Long id = category.getId();

        defaultCategoryFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCategoryFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCategoryFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryName equals to
        defaultCategoryFiltering("categoryName.equals=" + DEFAULT_CATEGORY_NAME, "categoryName.equals=" + UPDATED_CATEGORY_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryName in
        defaultCategoryFiltering(
            "categoryName.in=" + DEFAULT_CATEGORY_NAME + "," + UPDATED_CATEGORY_NAME,
            "categoryName.in=" + UPDATED_CATEGORY_NAME
        );
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryName is not null
        defaultCategoryFiltering("categoryName.specified=true", "categoryName.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryNameContainsSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryName contains
        defaultCategoryFiltering("categoryName.contains=" + DEFAULT_CATEGORY_NAME, "categoryName.contains=" + UPDATED_CATEGORY_NAME);
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryName does not contain
        defaultCategoryFiltering(
            "categoryName.doesNotContain=" + UPDATED_CATEGORY_NAME,
            "categoryName.doesNotContain=" + DEFAULT_CATEGORY_NAME
        );
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryType equals to
        defaultCategoryFiltering("categoryType.equals=" + DEFAULT_CATEGORY_TYPE, "categoryType.equals=" + UPDATED_CATEGORY_TYPE);
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryType in
        defaultCategoryFiltering(
            "categoryType.in=" + DEFAULT_CATEGORY_TYPE + "," + UPDATED_CATEGORY_TYPE,
            "categoryType.in=" + UPDATED_CATEGORY_TYPE
        );
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryType is not null
        defaultCategoryFiltering("categoryType.specified=true", "categoryType.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where createdAt equals to
        defaultCategoryFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllCategoriesByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where createdAt in
        defaultCategoryFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllCategoriesByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where createdAt is not null
        defaultCategoryFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where updatedAt equals to
        defaultCategoryFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllCategoriesByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where updatedAt in
        defaultCategoryFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllCategoriesByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where updatedAt is not null
        defaultCategoryFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryIconIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryIcon equals to
        defaultCategoryFiltering("categoryIcon.equals=" + DEFAULT_CATEGORY_ICON, "categoryIcon.equals=" + UPDATED_CATEGORY_ICON);
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryIconIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryIcon in
        defaultCategoryFiltering(
            "categoryIcon.in=" + DEFAULT_CATEGORY_ICON + "," + UPDATED_CATEGORY_ICON,
            "categoryIcon.in=" + UPDATED_CATEGORY_ICON
        );
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryIconIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryIcon is not null
        defaultCategoryFiltering("categoryIcon.specified=true", "categoryIcon.specified=false");
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryIconContainsSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryIcon contains
        defaultCategoryFiltering("categoryIcon.contains=" + DEFAULT_CATEGORY_ICON, "categoryIcon.contains=" + UPDATED_CATEGORY_ICON);
    }

    @Test
    @Transactional
    void getAllCategoriesByCategoryIconNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        // Get all the categoryList where categoryIcon does not contain
        defaultCategoryFiltering(
            "categoryIcon.doesNotContain=" + UPDATED_CATEGORY_ICON,
            "categoryIcon.doesNotContain=" + DEFAULT_CATEGORY_ICON
        );
    }

    @Test
    @Transactional
    void getAllCategoriesByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            categoryRepository.saveAndFlush(category);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        category.setUser(user);
        categoryRepository.saveAndFlush(category);
        Long userId = user.getId();
        // Get all the categoryList where user equals to userId
        defaultCategoryShouldBeFound("userId.equals=" + userId);

        // Get all the categoryList where user equals to (userId + 1)
        defaultCategoryShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultCategoryFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCategoryShouldBeFound(shouldBeFound);
        defaultCategoryShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCategoryShouldBeFound(String filter) throws Exception {
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
            .andExpect(jsonPath("$.[*].categoryName").value(hasItem(DEFAULT_CATEGORY_NAME)))
            .andExpect(jsonPath("$.[*].categoryType").value(hasItem(DEFAULT_CATEGORY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].categoryIcon").value(hasItem(DEFAULT_CATEGORY_ICON)));

        // Check, that the count call also returns 1
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCategoryShouldNotBeFound(String filter) throws Exception {
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCategoryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCategory() throws Exception {
        // Get the category
        restCategoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCategory() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the category
        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCategory are not directly saved in db
        em.detach(updatedCategory);
        updatedCategory
            .categoryName(UPDATED_CATEGORY_NAME)
            .categoryType(UPDATED_CATEGORY_TYPE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .categoryIcon(UPDATED_CATEGORY_ICON);

        restCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCategory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedCategory))
            )
            .andExpect(status().isOk());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCategoryToMatchAllProperties(updatedCategory);
    }

    @Test
    @Transactional
    void putNonExistingCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        category.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, category.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(category))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        category.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(category))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        category.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(category)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the category using partial update
        Category partialUpdatedCategory = new Category();
        partialUpdatedCategory.setId(category.getId());

        partialUpdatedCategory.categoryType(UPDATED_CATEGORY_TYPE);

        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCategory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCategory))
            )
            .andExpect(status().isOk());

        // Validate the Category in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCategoryUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCategory, category), getPersistedCategory(category));
    }

    @Test
    @Transactional
    void fullUpdateCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the category using partial update
        Category partialUpdatedCategory = new Category();
        partialUpdatedCategory.setId(category.getId());

        partialUpdatedCategory
            .categoryName(UPDATED_CATEGORY_NAME)
            .categoryType(UPDATED_CATEGORY_TYPE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .categoryIcon(UPDATED_CATEGORY_ICON);

        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCategory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCategory))
            )
            .andExpect(status().isOk());

        // Validate the Category in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCategoryUpdatableFieldsEquals(partialUpdatedCategory, getPersistedCategory(partialUpdatedCategory));
    }

    @Test
    @Transactional
    void patchNonExistingCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        category.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, category.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(category))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        category.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(category))
            )
            .andExpect(status().isBadRequest());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        category.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(category)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Category in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCategory() throws Exception {
        // Initialize the database
        insertedCategory = categoryRepository.saveAndFlush(category);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the category
        restCategoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, category.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return categoryRepository.count();
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

    protected Category getPersistedCategory(Category category) {
        return categoryRepository.findById(category.getId()).orElseThrow();
    }

    protected void assertPersistedCategoryToMatchAllProperties(Category expectedCategory) {
        assertCategoryAllPropertiesEquals(expectedCategory, getPersistedCategory(expectedCategory));
    }

    protected void assertPersistedCategoryToMatchUpdatableProperties(Category expectedCategory) {
        assertCategoryAllUpdatablePropertiesEquals(expectedCategory, getPersistedCategory(expectedCategory));
    }
}
