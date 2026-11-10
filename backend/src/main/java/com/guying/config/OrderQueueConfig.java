package com.guying.config;

import com.guying.common.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry", name = "enabled", havingValue = "true")
public class OrderQueueConfig {

    @Bean
    public DirectExchange orderDirectExchange() {
        return new DirectExchange(MqConstants.ORDER_DIRECT);
    }

    @Bean
    public Queue orderDeleteDocQueue() {
        return new Queue(MqConstants.ORDER_DELETE_DOC_QUEUE);
    }

    @Bean
    public Binding orderDeleteDocBinding() {
        return new Binding(MqConstants.ORDER_DELETE_DOC_QUEUE, Binding.DestinationType.QUEUE, MqConstants.ORDER_DIRECT, MqConstants.ORDER_DELETE_DOC_QUEUE, null);
    }

}
