package com.qiujie.agent.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qiujie.service.OrderService;
import com.qiujie.vo.OrderVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 订单查询工具
 *
 * @author qiujie
 */
@Component
public class QueryOrderTool {

    private final OrderService orderService;

    public QueryOrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    public String execute(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        int userId = args.getInt("userId", 0);
        if (userId <= 0) return "请先登录后再查询订单";
        List<OrderVO> orders = orderService.list(userId);
        if (orders == null || orders.isEmpty()) return "您还没有订单";
        List<Map<String, Object>> summary = orders.stream().map(o -> Map.<String, Object>of(
            "orderSn", o.getOrderSn() != null ? o.getOrderSn() : "",
            "status", o.getStatus() != null ? o.getStatus().getMessage() : "未知",
            "totalAmount", o.getTotalAmount() != null ? o.getTotalAmount().toString() : "0",
            "createTime", o.getCreateTime() != null ? o.getCreateTime().toString() : ""
        )).toList();
        return JSONUtil.toJsonStr(summary);
    }

    public static Map<String, Object> toolDef() {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", "queryOrders",
                "description", "查询用户的最近订单列表，包括订单号、状态、金额、时间",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "userId", Map.of("type", "integer", "description", "用户ID，从对话上下文中获取")
                    ),
                    "required", List.of("userId")
                )
            )
        );
    }
}
