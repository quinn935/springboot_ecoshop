package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.model.Category;
import com.ecommerce.sbecom.respositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service//tell spring to inject service as a bean
public class CategoryServiceImpl implements CategoryService {
    private List<Category> categories = new ArrayList<>();
    private Long nextId = 1L;

    @Autowired
    private CategoryRepository categoryRepository;
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        category.setCategoryId(nextId++);
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.delete(category);
        return "Category with categoryId " + categoryId + " deleted successfully.";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category savedCategory = categoryRepository.findById(categoryId)
                        .orElseThrow( ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found") );
        category.setCategoryId(categoryId);
        savedCategory = categoryRepository.save(category);
        return savedCategory;

//        }
    }
}
