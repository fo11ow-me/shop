package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Order;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.service.OrderService;
import com.qiujie.util.SecurityUtil;
import com.qiujie.vo.OrderVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("portalOrderController")
@RequestMapping("/portal/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseDTO<Order> create(@RequestBody Map<String, Object> params) {
        return Response.success(orderService.createFromCart(SecurityUtil.getCurrentUserId(), params));
    }

    @PostMapping("/buyNow")
    public ResponseDTO<Order> buyNow(@RequestBody Map<String, Object> params) {
        return Response.success(orderService.buyNow(SecurityUtil.getCurrentUserId(), params));
    }

    @PutMapping("/pay/{id}")
    public ResponseDTO<Void> pay(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> params) {
        Integer payMethod = params != null ? (Integer) params.getOrDefault("payMethod", 0) : 0;
        orderService.pay(SecurityUtil.getCurrentUserId(), id, payMethod);
        return Response.ok("支付成功");
    }

    @GetMapping("/list")
    public ResponseDTO<List<OrderVO>> list(@RequestParam(required = false) Integer status) {
        Integer userId = SecurityUtil.getCurrentUserId();
        if (status != null) {
            return Response.success(orderService.listByStatus(userId, status));
        }
        return Response.success(orderService.list(userId));
    }

    @GetMapping("/detail/{id}")
    public ResponseDTO<OrderVO> detail(@PathVariable Integer id) {
        return Response.success(orderService.detail(SecurityUtil.getCurrentUserId(), id));
    }

    @PutMapping("/cancel/{id}")
    public ResponseDTO<Void> cancel(@PathVariable Integer id) {
        OrderVO order = orderService.detail(SecurityUtil.getCurrentUserId(), id);
        if (order == null) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        orderService.cancel(id);
        return Response.ok("取消成功");
    }

    @PutMapping("/receipt/{id}")
    public ResponseDTO<Void> receipt(@PathVariable Integer id) {
        orderService.receipt(SecurityUtil.getCurrentUserId(), id);
        return Response.ok("确认收货成功");
    }

    @PutMapping("/recipient/{id}")
    public ResponseDTO<Void> updateRecipient(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        Integer expressDelivery = params.containsKey("expressDelivery") ? Integer.valueOf(params.get("expressDelivery")) : null;
        orderService.updateRecipient(SecurityUtil.getCurrentUserId(), id,
                params.get("recipientName"), params.get("recipientPhone"), params.get("recipientAddress"), expressDelivery);
        return Response.ok("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        orderService.delete(SecurityUtil.getCurrentUserId(), id);
        return Response.ok("删除成功");
    }
}
