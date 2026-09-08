package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiujie.entity.Category;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.CategoryMapper;
import com.qiujie.service.CategoryService;
import com.qiujie.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qiujie.constants.RedisConstants.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisUtil redisUtil;

    public CategoryServiceImpl(CategoryMapper categoryMapper, RedisUtil redisUtil) {
        this.categoryMapper = categoryMapper;
        this.redisUtil = redisUtil;
    }

    @Override
    public List<Category> tree() {
        List<Category> all = all();
        List<Category> parents = new java.util.ArrayList<>();
        java.util.Map<Integer, List<Category>> childrenMap = new java.util.HashMap<>();
        for (Category c : all) {
            if (c.getParentId() == 0) {
                parents.add(c);
            } else {
                childrenMap.computeIfAbsent(c.getParentId(), k -> new java.util.ArrayList<>()).add(c);
            }
        }
        for (Category parent : parents) {
            parent.setChildren(childrenMap.getOrDefault(parent.getId(), java.util.List.of()));
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
        redisUtil.del(CACHE_CATEGORY_TREE_KEY);
        redisUtil.del(CACHE_HOME_KEY);
    }

    @Override
    public void edit(Category category) {
        categoryMapper.updateById(category);
        redisUtil.del(CACHE_CATEGORY_TREE_KEY);
        redisUtil.del(CACHE_HOME_KEY);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.deleteById(id);
        redisUtil.del(CACHE_CATEGORY_TREE_KEY);
        redisUtil.del(CACHE_HOME_KEY);
    }
}
