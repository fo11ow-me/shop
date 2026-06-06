package com.qiujie.controller.admin;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Category;
import com.qiujie.service.CategoryService;
import com.qiujie.util.RedisUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.qiujie.constants.RedisConstants.*;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService categoryService;
    private final RedisUtil redisUtil;

    public CategoryController(CategoryService categoryService, RedisUtil redisUtil) {
        this.categoryService = categoryService;
        this.redisUtil = redisUtil;
    }

    @GetMapping("/tree")
    public ResponseDTO<List<Category>> tree() {
        return Response.success(categoryService.tree());
    }

    @GetMapping("/all")
    public ResponseDTO<List<Category>> all() {
        return Response.success(categoryService.all());
    }

    @PostMapping
    public ResponseDTO<Void> add(@RequestBody Category category) {
        categoryService.add(category);
        redisUtil.del(CACHE_CATEGORY_TREE_KEY);
        redisUtil.del(CACHE_HOME_KEY);
        return Response.ok("新增成功");
    }

    @PutMapping
    public ResponseDTO<Void> edit(@RequestBody Category category) {
        categoryService.edit(category);
        redisUtil.del(CACHE_CATEGORY_TREE_KEY);
        redisUtil.del(CACHE_HOME_KEY);
        return Response.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        redisUtil.del(CACHE_CATEGORY_TREE_KEY);
        redisUtil.del(CACHE_HOME_KEY);
        return Response.ok("删除成功");
    }
}
