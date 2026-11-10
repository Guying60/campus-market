package com.guying.common.enums;

import com.guying.pojo.OrderItem;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;

public enum OrderItemEnum {
    PAID(1, OrderItem::setPayTime),
    DELIVERED(2, OrderItem::setDeliveryTime),
    COMPLETED(3, OrderItem::setCompleteTime),
    CANCELLED(4, OrderItem::setCancelTime);


    private int code;
    private final BiConsumer<OrderItem, LocalDateTime> timeSetter;

    OrderItemEnum(int code, BiConsumer<OrderItem, LocalDateTime> timeSetter) {
        this.code = code;
        this.timeSetter = timeSetter;
    }

    public void apply(OrderItem orderItem, LocalDateTime time) {
        this.timeSetter.accept(orderItem,time);
    }

    public static OrderItemEnum getByCode(int code) {
        for (OrderItemEnum item : OrderItemEnum.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return CANCELLED;
    }
}
