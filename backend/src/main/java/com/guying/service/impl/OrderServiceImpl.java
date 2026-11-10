package com.guying.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guying.common.constants.MqConstants;
import com.guying.common.constants.ProductStatus;
import com.guying.common.enums.OrderItemEnum;
import com.guying.converter.OrderConverter;
import com.guying.dto.OrderDTO;
import com.guying.dto.OrderVO;
import com.guying.exception.ServiceException;
import com.guying.mapper.OrderItemMapper;
import com.guying.mapper.OrderMapper;
import com.guying.mapper.UserMapper;
import com.guying.mq.message.OrderCheckMessage;
import com.guying.mq.message.OrderEventDTO;
import com.guying.pojo.Orders;
import com.guying.pojo.OrderItem;
import com.guying.pojo.Product;
import com.guying.pojo.User;
import com.guying.service.OrderService;
import com.guying.service.ProductService;
import com.guying.service.UserService;
import com.guying.utils.OrderUtils;
import com.guying.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.guying.common.constants.MqConstants.DELAY_LEVELS;
import static com.guying.common.constants.RedisConstants.*;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrderConverter orderConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO.CreateResponse create(OrderDTO.CreateRequest createRequest) {
        Integer productId = createRequest.getProductId();
        Integer userId = UserContext.getUserId(); // 获取当前用户ID
        Product product =productService.getProductById(productId);
        if (product.getUserId().equals(userId)){
            throw new ServiceException("不能购买自己的商品");
        }
        //设置商品状态为出售,同时解决并发问题
        productService.updateStatus(ProductStatus.SALE,productId);
        //发送删除ES的Doc消息
        rabbitTemplate.convertAndSend(MqConstants.ORDER_DIRECT,MqConstants.ORDER_DELETE_ROUTING_KEY,productId);
        //获取买卖家信息
        Integer sellerId = product.getUserId();
        User seller = userService.getUserById(sellerId);
        User buyer = userService.getUserById(userId);
        //创建订单
        Orders orders = new Orders();
        orders.setUserId(userId); //设置买家ID
        orders.setProductId(productId);  //设置商品ID
        orders.setSellerId(sellerId);  //设置卖家ID
        orders.setPrice(product.getPrice());  //设置订单展示的商品价格
        orders.setStatus(0);  //设置订单状态为待付款
        orders.setOrderNo(OrderUtils.getOrderNo()); //设置订单编号
        orders.setAddress(createRequest.getAddress()); //设置买家收货地址
        orders.setCustomerPhone(createRequest.getPhone()); //设置买家手机号
        orders.setCustomerName(buyer.getNickname());
        orders.setProductTitle(product.getTitle());
        orders.setSellerName(seller.getNickname());//设置卖家昵称
        orders.setSellerPhone(seller.getPhone());//设置卖家手机号
        orders.setCreateTime(LocalDateTime.now());
        orders.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(orders);
        //创建订单副本
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(productId);
        orderItem.setOrderId(orders.getId());
        orderItem.setProductTitle(product.getTitle());
        orderItem.setPrice(product.getPrice());
        orderItem.setDescription(product.getDescription());
        orderItem.setStatus(0);
        orderItem.setImageUrl(product.getImageUrl());
        orderItem.setCreateTime(LocalDateTime.now());
        orderItemMapper.insert(orderItem);
        //返回OderVO
        OrderVO.CreateResponse createResponse = new OrderVO.CreateResponse();
        createResponse.setOrderId(orders.getId());
        log.info(userId +"订单创建完成:"+ orders.getOrderNo());
        //删除商品的缓存
        String key = REDIS_KEY_PREFIX_CACHE_PRODUCT + product.getId();
        String viewCountKey = REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT + product.getId();
        String priceKey=REDIS_KEY_PREFIX_CACHE_PRODUCT_PRICE+product.getId();
        if(StringUtils.hasText(key)){
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(viewCountKey);
            stringRedisTemplate.delete(priceKey);
        }
        //发送15分钟的定时消息
//        rabbitTemplate.convertAndSend(MqConstants.TTL_ORDER_DIRECT,MqConstants.TTL_ORDER_ROUTING_KEY,orders.getId());
        //用延迟消息
        sendOrderTimeoutMessage(orders.getId());
        return createResponse;
    }

    private void sendOrderTimeoutMessage(Integer orderId){
        OrderCheckMessage msg = new OrderCheckMessage(orderId, 0);
        rabbitTemplate.convertAndSend(MqConstants.DELAYED_ORDER_EXCHANGE,MqConstants.DELAYED_ORDER_ROUTING_KEY,msg,message->{
            message.getMessageProperties().setDelayLong(DELAY_LEVELS[0]);
            return message;
        });
    }

    //我买到的订单
    @Override
    public List<OrderVO.ListResponse> myOrderList() {
        Integer id = UserContext.getUserId();
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>().eq(Orders::getUserId, id));
        return orders.stream().map(order -> orderConverter.toListResponse(order)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Integer orderId) {
        updateOrderStatus(orderId,2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Integer orderId) {
        updateOrderStatus(orderId,3);
        Orders orders = orderMapper.selectById(orderId);
        //更新商品状态
        productService.updateStatus(ProductStatus.COMPLETE, orders.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Integer orderId) {
        updateOrderStatus(orderId,4);
        Orders orders = orderMapper.selectById(orderId);
        //更新商品状态
        productService.updateStatus(ProductStatus.OFF_SALE, orders.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Integer orderId) {
        updateOrderStatus(orderId,1);
    }

    @Override
    public OrderVO.DetailResponse detail(Integer orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId,orderId);
        OrderItem orderItem = orderItemMapper.selectOne(queryWrapper);
        return orderConverter.toDetailResponse(orderItem);
    }

    @Override
    public List<OrderVO.SoldListResponse> soldList() {
        Integer userId = UserContext.getUserId();
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>().eq(Orders::getSellerId, userId));
        return orders.stream().map(order -> orderConverter.toSoldListResponse(order)).collect(Collectors.toList());
    }

    /**
     * 待处理订单
     * @return
     */
    @Override
    public Long pendingOrder() {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, 1).eq(Orders::getSellerId, UserContext.getUserId()).or().eq(Orders::getStatus, 2).eq(Orders::getUserId, UserContext.getUserId());
        return orderMapper.selectCount(queryWrapper);
    }

    private void updateOrderStatus(Integer orderId, Integer status) {
        LocalDateTime updateTime = LocalDateTime.now();
        //订单
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setUpdateTime(updateTime);
        orders.setStatus(status);
        //订单副本
        OrderItem orderItem = new OrderItem();
        orderItem.setStatus(status);
//        if (status ==1){
//            orderItem.setPayTime(updateTime);
//        }
//        else if (status==2){
//            orderItem.setDeliveryTime(updateTime);
//        }
//        else if (status==3){
//            orderItem.setCompleteTime(updateTime);
//        }
//        else{
//            orderItem.setCancelTime(updateTime);
//        }
        OrderItemEnum.getByCode(status).apply(orderItem,updateTime);
        //更新DB
        orderMapper.updateById(orders);
        orderItemMapper.update(orderItem, new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }
}
