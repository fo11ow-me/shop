package com.qiujie.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.OrderService;
import com.qiujie.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "分页条件查询订单列表")
    @GetMapping("/list")
    public ResponseDTO<IPage<OrderVO>> list(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             String orderSn, String userName, String status,
                                             String startTime, String endTime) {
        return Response.success(orderService.list(current, size, orderSn, userName, status, startTime, endTime));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public ResponseDTO<OrderVO> detail(@PathVariable Integer id) {
        return Response.success(orderService.detail(id));
    }

    @Operation(summary = "发货")
    @PutMapping("/deliver/{id}")
    public ResponseDTO<Void> deliver(@PathVariable Integer id) {
        orderService.deliver(id);
        return Response.ok("发货成功");
    }

    @Operation(summary = "逻辑删除订单")
    @DeleteMapping("/delete/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        orderService.delete(id);
        return Response.success();
    }

    @Operation(summary = "批量逻辑删除订单")
    @DeleteMapping("/batch/{ids}")
    public ResponseDTO<Void> deleteBatch(@PathVariable List<Integer> ids) {
        orderService.batchDelete(ids);
        return Response.success();
    }

    @Operation(summary = "取消订单（待支付 -> 已取消）")
    @PutMapping("/cancel/{id}")
    public ResponseDTO<Void> cancel(@PathVariable Integer id) {
        orderService.cancel(id);
        return Response.ok("订单已取消");
    }

    @Operation(summary = "批量发货（已支付 -> 已发货）")
    @PutMapping("/batch-deliver/{ids}")
    public ResponseDTO<Void> batchDeliver(@PathVariable List<Integer> ids) {
        orderService.batchDeliver(ids);
        return Response.ok("批量发货成功");
    }
}
