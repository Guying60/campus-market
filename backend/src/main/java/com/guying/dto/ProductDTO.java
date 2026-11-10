package com.guying.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductDTO {
    @Data
    public static class SaveRequest {
        private Integer id;
        @NotBlank(message = "标题不能为空")
        private String title;
        @NotBlank(message = "描述不能为空")
        private String description;
        private Integer categoryId;
        @NotBlank(message = "图片不能为空")
        private String imageUrl;
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格至少为0.01元")
        @Digits(integer = 8, fraction = 2, message = "金额格式不正确，最多两位小数")
        private BigDecimal price;
        private Integer status; //(0 - 在售，1 - 已售出，2 - 已下架)
        @Digits(integer = 8, fraction = 2, message = "金额格式不正确，最多两位小数")
        private BigDecimal originalPrice;
        private LocalDateTime createTime;
    }

    @Data
    public static class ListRequest {
        private Integer page=1;
        @Min(value = 12, message = "size不能小于12")
        private Integer size=12;
        private String keyword;
        private Integer categoryId;
        //价格，发布时间
        private String sort;
        private String order;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }

    @Data
    public static class OrderBy {
        private String column;
        private Sort.Direction ascOrDesc;
    }


}
