package com.qiujie.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Category;
import com.qiujie.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SaCheckRole("admin")
@RestController
@RequestMapping("/admin/category")
@Tag(name = "管理端-分类管理")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public ResponseDTO<List<Category>> tree() {
        return Response.success(categoryService.tree());
    }

    @Operation(summary = "获取所有分类")
    @GetMapping("/all")
    public ResponseDTO<List<Category>> all() {
        return Response.success(categoryService.all());
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody Category category) {
        categoryService.add(category);
        return Response.ok("新增成功");
    }

    @Operation(summary = "修改分类")
    @PutMapping
    public ResponseDTO<Void> edit(@Valid @RequestBody Category category) {
        categoryService.edit(category);
        return Response.ok("修改成功");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return Response.ok("删除成功");
    }
}
