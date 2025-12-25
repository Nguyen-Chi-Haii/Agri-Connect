package vn.agriconnect.API.service;

import vn.agriconnect.API.model.Category;

import java.util.List;

public interface CategoryService {
    Category create(Category category);
    Category getById(String categoryId);
    Category update(String categoryId, Category category);
    void delete(String categoryId);
    List<Category> getAll();
    List<Category> getByParent(String parentId);
}
