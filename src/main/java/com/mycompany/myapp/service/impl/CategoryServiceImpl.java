package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.service.CategoryService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Category}.
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category save(Category category) {
        LOG.debug("Request to save Category : {}", category);
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Category category) {
        LOG.debug("Request to update Category : {}", category);
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

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findOne(Long id) {
        LOG.debug("Request to get Category : {}", id);
        return categoryRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Category : {}", id);
        categoryRepository.deleteById(id);
    }
}
