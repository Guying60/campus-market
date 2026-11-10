package com.guying.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductVO {

    @Data
    public static class ListResponse {
        private Integer id;
        private String title;
        private String description;
        private String imageUrl;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer categoryId;
    }

    @Data
    public static class myListResponse {
        private Integer id;
        private String title;
        private String description;
        private String imageUrl;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer status;
        private Integer categoryId;
    }

    @Data
    public static class DetailResponse {
        private Integer id;
        private String title;
        private String description;
        private Integer userId;
        private String imageUrl;
        private BigDecimal originalPrice;
        private Integer status;
        private Integer categoryId;
        private Integer sellerId;
        private String sellerName;
        private String sellerPhone;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime updateTime;
    }

    @Data
    public static class StatusResponse {
        private Integer id;
        private BigDecimal price;
        private Integer viewCount;
    }
}
