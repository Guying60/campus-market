package com.guying.controller;

import com.guying.dto.OrderDTO;
import com.guying.dto.OrderVO;
import com.guying.pojo.Result;
import com.guying.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result create(@RequestBody @Validated OrderDTO.CreateRequest createRequest) {
        log.info("创建订单");
        OrderVO.CreateResponse createResponse = orderService.create(createRequest);
        return Result.success(createResponse);
    }

    @GetMapping("/list")
    public Result list(){
        log.info("查询订单记录");
        return Result.success(orderService.myOrderList());
    }

    @GetMapping("/sold-list")
    public Result soldList(){
        log.info("查询我的卖掉订单列表");
        return Result.success(orderService.soldList());
    }

    @GetMapping("/detail/{orderId}")
    public Result detail(@PathVariable Integer orderId){
        log.info("查询订单详情");
        return Result.success(orderService.detail(orderId));
    }

    @PostMapping("/ship/{orderId}")
    public Result ship(@PathVariable Integer orderId){
        log.info("发货");
        orderService.ship(orderId);
        return Result.success();
    }

    @PostMapping("/confirm/{orderId}")
    public Result confirm(@PathVariable Integer orderId){
        log.info("确认收货");
        orderService.confirm(orderId);
        return Result.success();
    }

    @PostMapping("/cancel/{orderId}")
    public Result cancel(@PathVariable Integer orderId){
        log.info("取消订单");
        orderService.cancel(orderId);
        return Result.success();
    }

    @PostMapping("/pay/{orderId}")
    public Result pay(@PathVariable Integer orderId){
        log.info("支付订单");
        orderService.pay(orderId);
        return Result.success();
    }

    @GetMapping("/pending-order")
    public Result pendingOrder(){
        log.info("查询待处理订单列表");
        Long count = orderService.pendingOrder();
        return Result.success(count);
    }
}
