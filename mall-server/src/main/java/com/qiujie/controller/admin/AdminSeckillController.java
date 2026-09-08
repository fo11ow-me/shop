package com.qiujie.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.ProductImg;
import com.qiujie.entity.SeckillSession;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SaCheckRole("admin")
@Tag(name = "秒杀管理")
@Profile("!test")
@RestController
@RequestMapping("/admin/seckill")
public class AdminSeckillController {

    private final SeckillService seckillService;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;

    public AdminSeckillController(SeckillService seckillService,
                                   ProductMapper productMapper,
                                   ProductImgMapper productImgMapper) {
        this.seckillService = seckillService;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
    }

    @Operation(summary = "秒杀场次分页列表")
    @GetMapping("/list")
    public ResponseDTO<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Integer status) {
        return Response.success(seckillService.listPage(current, size, status));
    }

    @Operation(summary = "创建秒杀场次")
    @PostMapping
    public ResponseDTO<Void> create(@Valid @RequestBody SeckillSession session) {
        seckillService.create(session);
        return Response.ok("创建成功");
    }

    @Operation(summary = "更新秒杀场次")
    @PutMapping
    public ResponseDTO<Void> update(@Valid @RequestBody SeckillSession session) {
        seckillService.updateSession(session);
        return Response.ok("更新成功");
    }

    @Operation(summary = "删除秒杀场次")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        seckillService.deleteSession(id);
        return Response.ok("删除成功");
    }

    @Operation(summary = "商品选择器（供秒杀弹窗下拉框使用）")
    @GetMapping("/product-options")
    public ResponseDTO<List<Map<String, Object>>> productOptions() {
        List<com.qiujie.entity.Product> products = productMapper.selectList(null);
        Map<Integer, ProductImg> imgMap = productImgMapper.selectByProductIds(
                products.stream().map(p -> p.getId()).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(ProductImg::getProductId, img -> img, (a, b) -> a));
        List<Map<String, Object>> list = products.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("price", p.getPrice());
            ProductImg img = imgMap.get(p.getId());
            m.put("image", img != null ? img.getUrl() : null);
            return m;
        }).collect(Collectors.toList());
        return Response.success(list);
    }
}
