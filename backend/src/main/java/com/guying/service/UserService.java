package com.guying.service;

import com.guying.dto.UserDTO;
import com.guying.dto.UserVO;
import com.guying.pojo.User;

public interface UserService {
    UserVO.LoginResponse login(UserDTO.LoginRequest loginRequest);

    void register(UserDTO.RegisterRequest user);

    UserVO.InfoResponse getUserInfo();

    void update(UserDTO.UpdateRequest updateRequest);

    User getUserById(Integer id);

    void logout(String token);

    UserVO.TokenVo refreshToken(UserDTO.RefreshTokenDto refreshTokenDto);
}
