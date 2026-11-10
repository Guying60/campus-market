package com.guying.mq.consumer;

import com.guying.common.constants.MqConstants;
import com.guying.mq.message.OrderCheckMessage;
import com.guying.pojo.Orders;
import com.guying.pojo.ProductDoc;
import com.guying.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderMessageListener {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ElasticsearchOperations operations;


    /**
     * 处理订单延迟消息,删除超时订单
     * @param msg
     */
    @RabbitListener(queues = MqConstants.DELAYED_ORDER_QUEUE)
    public void handleOrderDelayedMessage(OrderCheckMessage msg){
        log.info("处理订单延迟消息: {}", msg.getOrderId());
        Integer orderId = msg.getOrderId();
        int level = msg.getLevel();
        Orders order = orderService.lambdaQuery()
                .eq(Orders::getId, orderId)
                .one();
        if (order == null || order.getStatus() != 0) {
            log.error("订单不存在: {}", orderId);
            return;
        }
        if (level >= MqConstants.DELAY_LEVELS.length-1) {
            orderService.lambdaUpdate()
                    .eq(Orders::getId, orderId)
                    .eq(Orders::getStatus, 0)
                    .set(Orders::getStatus, 4)
                    .update();
            //TODO 发送给用户通知
            return;
        }
        int nextLevel = level + 1;
        OrderCheckMessage nextMessage = new OrderCheckMessage(orderId, nextLevel);
        rabbitTemplate.convertAndSend(MqConstants.DELAYED_ORDER_EXCHANGE, MqConstants.DELAYED_ORDER_ROUTING_KEY, nextMessage, message -> {
            message.getMessageProperties().setDelayLong(MqConstants.DELAY_LEVELS[nextLevel]);
            return message;
        });
    }

    /**
     * 处理删除文档消息
     * @param productId
     */
    @RabbitListener(queues = MqConstants.ORDER_DELETE_DOC_QUEUE)
    public void handleDeleteDocumentMessage(Integer productId) {
        log.info("处理删除文档消息,DocId: {}", productId);
        operations.delete(String.valueOf(productId), ProductDoc.class);
    }
}
