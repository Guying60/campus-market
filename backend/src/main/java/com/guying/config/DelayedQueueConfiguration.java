package com.guying.config;

import com.guying.common.constants.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry", name = "enabled", havingValue = "true")
public class DelayedQueueConfiguration {
    /**
     * 声明延迟交换机
     * @return
     */
    @Bean
    public CustomExchange delayedExchange(){
        Map<String, Object> args = Map.of("x-delayed-type", "direct");
        return new CustomExchange(MqConstants.DELAYED_ORDER_EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * 声明延迟队列
     * @return
     */
    @Bean
    public Queue delayedQueue(){
        return QueueBuilder.durable(MqConstants.DELAYED_ORDER_QUEUE).build();
    }

    /**
     * 绑定延迟队列到延迟交换机
     * @return
     */
    @Bean
    public Binding delayedBinding(){
        return BindingBuilder.bind(delayedQueue()).to(delayedExchange()).with(MqConstants.DELAYED_ORDER_ROUTING_KEY).noargs();
    }













    /**
    * 死信队列
    * @return
    */
    /*
    /**
     * 声明TTL队列
     * @return
     * /
    @Bean
    public Queue ttlQueue(){
        return QueueBuilder.durable(MqConstants.TTL_ORDER_QUEUE)
                .ttl(MqConstants.TTL_ORDER_EXPIRE_TIME)
                .deadLetterExchange(MqConstants.DLX_ORDER_DIRECT)
                .deadLetterRoutingKey(MqConstants.DLX_ORDER_ROUTING_KEY)
                .build();
    }

    /**
     * 声明TTL交换机
     * @return
     * /
    @Bean
    public DirectExchange ttlExchange(){
        return ExchangeBuilder.directExchange(MqConstants.TTL_ORDER_DIRECT)
                .durable(true)
                .build();
    }
    /**
     * 绑定TTL队列到TTL交换机
     * @return
     * /
    @Bean
    public Binding ttlBinding(){
        return BindingBuilder.bind(ttlQueue()).to(ttlExchange()).with(MqConstants.TTL_ORDER_ROUTING_KEY);
    }


    /**
     * 声明死信队列
     * @return
     * /
    @Bean
    public Queue deadLetterQueue(){
        return QueueBuilder.durable(MqConstants.DLX_ORDER_QUEUE)
                .build();
    }

    /**
     * 声明死信交换机
     * @return
     * /
    @Bean
    public DirectExchange dlxExchange(){
        return ExchangeBuilder.directExchange(MqConstants.DLX_ORDER_DIRECT)
                .durable(true)
                .build();
    }

    /**
     * 绑定死信队列到死信交换机
     * @return
     * /
    @Bean
    public Binding dlxBinding(){
        return BindingBuilder.bind(deadLetterQueue()).to(dlxExchange()).with(MqConstants.DLX_ORDER_ROUTING_KEY);
    }
    */
}
