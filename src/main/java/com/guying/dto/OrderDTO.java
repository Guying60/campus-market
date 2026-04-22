package com.guying.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class OrderDTO {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateRequest {
        private Integer productId;
        @NotBlank(message = "地址不能为空")
        @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9-]{2,38}$", message = "地址格式不正确")
        private String address;
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }
}
