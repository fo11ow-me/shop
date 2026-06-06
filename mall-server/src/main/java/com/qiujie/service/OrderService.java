package com.qiujie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.Order;
import com.qiujie.vo.OrderVO;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    Order createFromCart(Integer userId, Map<String, Object> params);

    Order buyNow(Integer userId, Map<String, Object> params);

    void pay(Integer userId, Integer id, Integer payMethod);

    List<OrderVO> list(Integer userId);

    List<OrderVO> listByStatus(Integer userId, Integer status);

    OrderVO detail(Integer userId, Integer id);

    void delete(Integer userId, Integer id);

    IPage<OrderVO> list(Integer current, Integer size, String orderSn, String userName, String status,
                        String startTime, String endTime);

    OrderVO detail(Integer id);

    void deliver(Integer id);

    void cancel(Integer id);

    void receipt(Integer userId, Integer id);

    void updateRecipient(Integer userId, Integer id, String recipientName, String recipientPhone, String recipientAddress, Integer expressDelivery);

    void batchDeliver(List<Integer> ids);

    void delete(Integer id);

    void batchDelete(List<Integer> ids);
}
