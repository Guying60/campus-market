package com.guying.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guying.dto.OrderDTO;
import com.guying.dto.OrderVO;
import com.guying.pojo.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {
    OrderVO.CreateResponse create(OrderDTO.CreateRequest createRequest);

    List<OrderVO.ListResponse> myOrderList();

    void ship(Integer orderId);

    void confirm(Integer orderId);

    void cancel(Integer orderId);

    void pay(Integer orderId);

    OrderVO.DetailResponse detail(Integer orderId);

    List<OrderVO.SoldListResponse> soldList();

    Long pendingOrder();

}
