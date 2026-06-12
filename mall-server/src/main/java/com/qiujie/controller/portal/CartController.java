package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Cart;
import com.qiujie.service.CartService;
import cn.dev33.satoken.stp.StpUtil;
import com.qiujie.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/cart")
@Tag(name = "门户端-购物车")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "购物车列表")
    @GetMapping("/list")
    public ResponseDTO<List<CartVO>> list() {
        return Response.success(cartService.list(StpUtil.getLoginIdAsInt()));
    }

    @Operation(summary = "添加到购物车")
    @PostMapping("/add")
    public ResponseDTO<Void> add(@Valid @RequestBody Cart cart) {
        cartService.add(StpUtil.getLoginIdAsInt(), cart);
        return Response.ok("添加购物车成功");
    }

    @Operation(summary = "更新购物车")
    @PutMapping("/update")
    public ResponseDTO<Void> update(@Valid @RequestBody Cart cart) {
        cartService.update(StpUtil.getLoginIdAsInt(), cart);
        return Response.ok("更新成功");
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/delete/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        cartService.delete(StpUtil.getLoginIdAsInt(), id);
        return Response.ok("删除成功");
    }

    @Operation(summary = "批量删除购物车")
    @DeleteMapping("/batchDelete")
    public ResponseDTO<Void> batchDelete(@RequestBody Map<String, List<Integer>> params) {
        cartService.batchDelete(StpUtil.getLoginIdAsInt(), params);
        return Response.ok("批量删除成功");
    }
}
