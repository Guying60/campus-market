package com.guying.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class UserDTO {
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;

    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^(?![a-zA-Z]+$)(?!\\d+$)(?![!@#$%^&*.]+$)[a-zA-Z\\d!@#$%^&*.]{6,20}$", message = "密码格式不正确,密码必须包含字母、数字、特殊符号中的至少两种")
        private String password;
        @NotBlank(message = "昵称不能为空")
        @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,15}$", message = "昵称格式不正确,昵称只可以包含汉字数字字母下划线")
        private String nickname;
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank(message = "昵称不能为空")
        @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]{2,15}$", message = "昵称格式不正确")
        private String nickname;
        @NotBlank(message = "地址不能为空")
        @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9-]{2,38}$", message = "地址格式不正确")
        private String address;
        private String avatar;
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        @NotBlank(message = "邮箱不能为空")
        @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式不正确")
        private String email;
    }

    @Data
    public static class RefreshTokenDto {
        @NotBlank(message = "RefreshToken不能为空")
        private String refreshToken;
    }
}
