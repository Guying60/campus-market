package com.guying.converter; // 明确的包名

import com.guying.dto.UserDTO;
import com.guying.dto.UserVO;
import com.guying.pojo.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {
    
    UserVO.InfoResponse toInfoResponse(User user);
    UserVO.LoginResponse toLoginResponse(User user);
    User fromRegisterRequest(UserDTO.RegisterRequest registerRequest);
    User fromUpdateRequest(UserDTO.UpdateRequest updateRequest);
}