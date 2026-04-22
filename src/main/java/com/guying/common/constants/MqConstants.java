package com.guying.common.constants;

public class MqConstants {

    /**
     * 延迟消息队列
     */
    public static final String DELAYED_ORDER_EXCHANGE = "delayed.order.exchange";
    public static final String DELAYED_ORDER_QUEUE = "delayed.order.queue";
    public static final String DELAYED_ORDER_ROUTING_KEY = "delayed.order.cancel";
    public static final long[] DELAY_LEVELS = {
            10_000,   // 10s
            20_000,   // 20s  累计 30s
            30_000,   // 30s  累计 60s
            60_000,   // 1min 累计 2min
            120_000,  // 2min 累计 4min
            300_000,  // 5min 累计 9min
            360_000,  // 6min 累计 10min
    };

    /**
     * 订单消息队列
     */
    public static final String ORDER_DIRECT = "order.direct";
    public static final String ORDER_DELETE_DOC_QUEUE = "order.delete.doc.queue";
    public static final String ORDER_DELETE_ROUTING_KEY = "order.delete.doc";
}
