package com.guying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserVO {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String nickname;
        private String avatar;
        private Integer status;
        private String accessToken;
        private String refreshToken;
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class InfoResponse {
        private Integer id;
        private String username;
        private String nickname;
        private String avatar;
        private String phone;
        private String address;
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class TokenVo {
        private String accessToken;
    }
}
