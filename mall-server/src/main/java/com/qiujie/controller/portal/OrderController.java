package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Order;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.service.OrderService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("portalOrderController")
@RequestMapping("/portal/order")
@Tag(name = "门户端-订单")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "从购物车创建订单")
    @PostMapping("/create")
    public ResponseDTO<Order> create(@RequestBody Map<String, Object> params) {
        return Response.success(orderService.createFromCart(StpUtil.getLoginIdAsInt(), params));
    }

    @Operation(summary = "立即购买")
    @PostMapping("/buyNow")
    public ResponseDTO<Order> buyNow(@RequestBody Map<String, Object> params) {
        return Response.success(orderService.buyNow(StpUtil.getLoginIdAsInt(), params));
    }

    @Operation(summary = "支付订单")
    @PutMapping("/pay/{id}")
    public ResponseDTO<Void> pay(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> params) {
        Integer payMethod = params != null ? (Integer) params.getOrDefault("payMethod", 0) : 0;
        orderService.pay(StpUtil.getLoginIdAsInt(), id, payMethod);
        return Response.ok("支付成功");
    }

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public ResponseDTO<List<Order>> list(@RequestParam(required = false) Integer status) {
        Integer userId = StpUtil.getLoginIdAsInt();
        if (status != null) {
            return Response.success(orderService.listByStatus(userId, status));
        }
        return Response.success(orderService.list(userId));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public ResponseDTO<Order> detail(@PathVariable Integer id) {
        return Response.success(orderService.detail(StpUtil.getLoginIdAsInt(), id));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/cancel/{id}")
    public ResponseDTO<Void> cancel(@PathVariable Integer id) {
        Order order = orderService.detail(StpUtil.getLoginIdAsInt(), id);
        if (order == null) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        orderService.cancel(id);
        return Response.ok("取消成功");
    }

    @Operation(summary = "确认收货")
    @PutMapping("/receipt/{id}")
    public ResponseDTO<Void> receipt(@PathVariable Integer id) {
        orderService.receipt(StpUtil.getLoginIdAsInt(), id);
        return Response.ok("确认收货成功");
    }

    @Operation(summary = "更新收货信息")
    @PutMapping("/recipient/{id}")
    public ResponseDTO<Void> updateRecipient(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        Integer expressDelivery = params.containsKey("expressDelivery") ? Integer.valueOf(params.get("expressDelivery")) : null;
        orderService.updateRecipient(StpUtil.getLoginIdAsInt(), id,
                params.get("recipientName"), params.get("recipientPhone"), params.get("recipientAddress"), expressDelivery);
        return Response.ok("更新成功");
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/delete/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        orderService.delete(StpUtil.getLoginIdAsInt(), id);
        return Response.ok("删除成功");
    }
}
