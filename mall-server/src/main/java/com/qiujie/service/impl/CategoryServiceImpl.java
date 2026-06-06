package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiujie.entity.Category;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.CategoryMapper;
import com.qiujie.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> tree() {
        List<Category> parents = categoryMapper.selectByParentId(0);
        for (Category parent : parents) {
            List<Category> children = categoryMapper.selectByParentId(parent.getId());
            parent.setChildren(children);
        }
        return parents;
    }

    @Override
    public List<Category> all() {
        return categoryMapper.selectList(new QueryWrapper<Category>().eq("is_deleted", 0));
    }

    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    public void edit(Category category) {
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.deleteById(id);
    }
}
