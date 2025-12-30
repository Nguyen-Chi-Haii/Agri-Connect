package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.model.Category;
import vn.agriconnect.API.repository.CategoryRepository;
import vn.agriconnect.API.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category getById(String categoryId) {
        return categoryRepository.findById(categoryId).orElse(null);
    }

    @Override
    public Category update(String categoryId, Category category) {
        category.setId(categoryId);
        return categoryRepository.save(category);
    }

    @Override
    public void delete(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getByParent(String parentId) {
        return categoryRepository.findByParentId(parentId);
    }
}
