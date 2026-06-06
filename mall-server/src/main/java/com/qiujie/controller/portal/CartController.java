package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Cart;
import com.qiujie.service.CartService;
import com.qiujie.util.SecurityUtil;
import com.qiujie.vo.CartVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/list")
    public ResponseDTO<List<CartVO>> list() {
        return Response.success(cartService.list(SecurityUtil.getCurrentUserId()));
    }

    @PostMapping("/add")
    public ResponseDTO<Void> add(@RequestBody Cart cart) {
        cartService.add(SecurityUtil.getCurrentUserId(), cart);
        return Response.ok("添加购物车成功");
    }

    @PutMapping("/update")
    public ResponseDTO<Void> update(@RequestBody Cart cart) {
        cartService.update(SecurityUtil.getCurrentUserId(), cart);
        return Response.ok("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        cartService.delete(SecurityUtil.getCurrentUserId(), id);
        return Response.ok("删除成功");
    }

    @DeleteMapping("/batchDelete")
    public ResponseDTO<Void> batchDelete(@RequestBody Map<String, List<Integer>> params) {
        cartService.batchDelete(SecurityUtil.getCurrentUserId(), params);
        return Response.ok("批量删除成功");
    }
}
