package com.ly.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.reggie.entity.OrderDetail;
import com.ly.reggie.entity.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
    List<OrderDetail> getOrderDetailListByOrderId(Long orderId);
}
