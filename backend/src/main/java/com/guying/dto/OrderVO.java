package com.guying.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderVO {

    @Data
    public static class ListResponse {
        private Integer id;
        private String productTitle;
        private String orderNo;
        private BigDecimal price;
        private Integer status;
        private String sellerName;
        private String sellerPhone;
    }

    @Data
    public static class SoldListResponse {
        private Integer id;
        private String orderNo;
        private String productTitle;
        private BigDecimal price;
        private Integer status;
        private String customerName;
        private String address;
        private String customerPhone;
    }

    @Data
    public static class CreateResponse{
        private Integer orderId;
    }

    @Data
    public static class DetailResponse {
        private Integer id;
        private String productTitle;
        private BigDecimal price;
        private String imageUrl;
        private String description;
        private Integer status;//(0 - 待付款，1 - 待发货，2 - 待收货，3 - 已完成，4 - 已取消)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime payTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime deliveryTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime completeTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime cancelTime;
    }
}
