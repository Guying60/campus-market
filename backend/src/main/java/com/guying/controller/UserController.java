package com.guying.controller;

import com.guying.dto.UserDTO;
import com.guying.dto.UserVO;
import com.guying.pojo.Result;
import com.guying.pojo.User;
import com.guying.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody @Validated UserDTO.LoginRequest loginRequest) {
        log.info("用户名: {}", loginRequest.getUsername());
        UserVO.LoginResponse loginResponse = userService.login(loginRequest);
        return Result.success(loginResponse);
    }

    @PostMapping("/register")
    public Result register(@RequestBody @Validated UserDTO.RegisterRequest registerRequest) {
        log.info("用户名: {}, 密码: {}", registerRequest.getUsername(), registerRequest.getPassword());
        userService.register(registerRequest);
        return Result.success();
    }

    @GetMapping("/info")
    public Result info() {
        log.info("获取用户信息");
        UserVO.InfoResponse infoResponse =userService.getUserInfo();
        return Result.success(infoResponse);
    }

    @PostMapping("/update")
    public Result update(@RequestBody @Validated UserDTO.UpdateRequest updateRequest) {
        log.info("更新用户信息");
        userService.update(updateRequest);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        log.info("用户退出登录");
        String token = request.getHeader("Authorization");
        userService.logout(token);
        return Result.success();
    }

    @PostMapping("/refresh-token")
    public Result refreshToken(@RequestBody @Validated UserDTO.RefreshTokenDto refreshTokenDto) {
        UserVO.TokenVo tokenVo =userService.refreshToken(refreshTokenDto);
        return Result.success(tokenVo);
    }
}
