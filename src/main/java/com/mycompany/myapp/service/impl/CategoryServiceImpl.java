package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.BudgeStatus;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.service.CategoryService;
import com.mycompany.myapp.service.UserService;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.Category}.
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    @Override
    public Category save(Category category) {
        LOG.debug("Request to save Budget : {}", category);
        if (category.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> currentUser = userService.getUserWithAuthorities();
                currentUser.ifPresent(category::setUser);
            }
        }
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());

        return categoryRepository.save(category);
    }

    @Override
    public Category update(Category category) {
        LOG.debug("Request to save Budget : {}", category);
        if (category.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> currentUser = userService.getUserWithAuthorities();
                currentUser.ifPresent(category::setUser);
            }
        }
        category.setUpdatedAt(Instant.now());
        return categoryRepository.save(category);
    }

    @Override
    public Optional<Category> partialUpdate(Category category) {
        LOG.debug("Request to partially update Category : {}", category);

        return categoryRepository
            .findById(category.getId())
            .map(existingCategory -> {
                if (category.getCategoryName() != null) {
                    existingCategory.setCategoryName(category.getCategoryName());
                }
                if (category.getCategoryType() != null) {
                    existingCategory.setCategoryType(category.getCategoryType());
                }
                if (category.getCreatedAt() != null) {
                    existingCategory.setCreatedAt(category.getCreatedAt());
                }
                if (category.getUpdatedAt() != null) {
                    existingCategory.setUpdatedAt(category.getUpdatedAt());
                }
                if (category.getCategoryIcon() != null) {
                    existingCategory.setCategoryIcon(category.getCategoryIcon());
                }

                return existingCategory;
            })
            .map(categoryRepository::save);
    }

    public Page<Category> findAllWithEagerRelationships(Pageable pageable) {
        return categoryRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findOne(Long id) {
        LOG.debug("Request to get Category : {}", id);
        return categoryRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Category : {}", id);
        categoryRepository.deleteById(id);
    }
}
