package com.qiujie.service;

import com.qiujie.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CategoryServiceImpl tests")
class CategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("tree — returns full category tree")
    void shouldReturnCategoryTree() {
        List<Category> tree = categoryService.tree();

        assertNotNull(tree);
        assertFalse(tree.isEmpty());
        Category electronics = tree.stream()
                .filter(c -> c.getName().equals("电子产品")).findFirst().orElseThrow();
        assertNotNull(electronics.getChildren());
    }

    @Test
    @DisplayName("add — creates category under parent")
    void shouldAddCategory() {
        Category cat = new Category();
        cat.setName("平板电脑");
        cat.setParentId(1);

        categoryService.add(cat);

        assertNotNull(cat.getId());
        assertTrue(cat.getId() > 0);
    }

    @Test
    @DisplayName("edit — updates category name")
    void shouldEditCategory() {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("电子产品-改");

        categoryService.edit(cat);

        List<Category> tree = categoryService.tree();
        assertTrue(tree.stream().anyMatch(c -> c.getName().equals("电子产品-改")));
    }

    @Test
    @DisplayName("delete — removes category")
    void shouldDeleteCategory() {
        categoryService.delete(3);

        List<Category> tree = categoryService.tree();
        assertTrue(tree.stream().noneMatch(c -> c.getId().equals(3)));
    }
}
