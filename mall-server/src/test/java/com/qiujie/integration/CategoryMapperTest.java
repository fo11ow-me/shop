package com.qiujie.integration;

import com.qiujie.entity.Category;
import com.qiujie.mapper.CategoryMapper;
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
@DisplayName("CategoryMapper integration tests")
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("selectByParentId — returns child categories")
    void shouldSelectByParentId() {
        List<Category> children = categoryMapper.selectByParentId(1);

        assertNotNull(children);
        assertFalse(children.isEmpty());
        assertTrue(children.stream().anyMatch(c -> c.getName().equals("手机")));
    }

    @Test
    @DisplayName("selectByParentId — returns empty for leaf categories")
    void shouldReturnEmptyForLeaf() {
        List<Category> children = categoryMapper.selectByParentId(2);
        assertTrue(children.isEmpty());
    }

    @Test
    @DisplayName("selectByParentId — returns top-level categories for parentId=0")
    void shouldReturnTopLevelCategories() {
        List<Category> topLevel = categoryMapper.selectByParentId(0);

        assertNotNull(topLevel);
        assertTrue(topLevel.size() >= 2);
    }
}
