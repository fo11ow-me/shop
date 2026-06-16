package com.qiujie.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Product;
import com.qiujie.service.ImageService;
import com.qiujie.service.ProductAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@SaCheckRole("admin")
@RestController("adminProductController")
@RequestMapping("/admin/product")
@Tag(name = "管理端-商品管理")
public class ProductController {

    private final ProductAdminService productAdminService;
    private final ImageService imageService;

    public ProductController(ProductAdminService productAdminService, ImageService imageService) {
        this.productAdminService = productAdminService;
        this.imageService = imageService;
    }

    @Operation(summary = "获取商品图片")
    @GetMapping("/img")
    public void image(@RequestParam("key") String key, HttpServletResponse response) throws IOException {
        imageService.serveProductImage(key, response);
    }

    @Operation(summary = "商品分页列表")
    @GetMapping("/list")
    public ResponseDTO<IPage<Product>> list(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             String name, Integer status, Integer categoryId) {
        return Response.success(productAdminService.listPage(current, size, name, status, categoryId));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody Product product) {
        productAdminService.add(product);
        return Response.ok("新增成功");
    }

    @Operation(summary = "修改商品")
    @PutMapping
    public ResponseDTO<Void> edit(@Valid @RequestBody Product product) {
        productAdminService.updateById(product);
        return Response.ok("修改成功");
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        productAdminService.removeById(id);
        return Response.ok("删除成功");
    }

    @Operation(summary = "更新商品状态")
    @PutMapping("/status/{id}")
    public ResponseDTO<Void> updateStatus(@PathVariable Integer id) {
        productAdminService.toggleStatus(id);
        return Response.ok("操作成功");
    }
}
