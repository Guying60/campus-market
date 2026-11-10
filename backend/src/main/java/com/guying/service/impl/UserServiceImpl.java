package com.guying.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guying.converter.UserConverter;
import com.guying.dto.UserDTO;
import com.guying.dto.UserVO;
import com.guying.exception.ServiceException;
import com.guying.mapper.UserMapper;
import com.guying.pojo.User;
import com.guying.service.UserService;
import com.guying.utils.CacheClient;
import com.guying.utils.JwtUtils;
import com.guying.utils.PasswordUtils;
import com.guying.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.concurrent.TimeUnit;

import static com.guying.common.constants.RedisConstants.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private CacheClient cacheClient;


    @Override
    public UserVO.LoginResponse login(UserDTO.LoginRequest loginRequest) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.select(User::getId,User::getAvatar,User::getNickname,User::getStatus,User::getPassword).eq(User::getUsername, loginRequest.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if (user==null){
            throw new ServiceException("未找到该账号，请确认账号是否输入正确");
        }
        //解密并验证密码
        String hashedPassword = user.getPassword();
        if (!PasswordUtils.checkPassword(loginRequest.getPassword(), hashedPassword)){
            throw new ServiceException("密码错误");
        }
        //返回数据
        UserVO.LoginResponse loginResponse = userConverter.toLoginResponse(user);
        //生成双token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.createRefreshToken(user.getId());
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX_LOGIN_REFRESH_TOKEN+user.getId(), refreshToken,7, TimeUnit.DAYS);
        return loginResponse;
    }

    @Override
    public void register(UserDTO.RegisterRequest registerRequest) {
        //拆装DTO
        User user= userConverter.fromRegisterRequest(registerRequest);
        //设置状态为正常
        user.setStatus(1);
        //加密
        String originalPassword = registerRequest.getPassword();
        String hashedPassword = PasswordUtils.hashPassword(originalPassword);
        user.setPassword(hashedPassword);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        try {
            userMapper.insert(user);
            log.info("用户注册:"+user.getId());
        } catch (Exception e) {
            log.warn("注册失败,用户名已存在");
            throw new ServiceException("注册失败,用户名已存在");
        }
    }

    @Override
    public UserVO.InfoResponse getUserInfo() {
        Integer userId = UserContext.getUserId();
        return cacheClient.queryWithMutex(REDIS_KEY_PREFIX_CACHE_USERINFO,userId,
                UserVO.InfoResponse.class,dbId->{User user = userMapper.selectById(dbId);
                return userConverter.toInfoResponse(user);},30L,TimeUnit.MINUTES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserDTO.UpdateRequest updateRequest) {
        Integer userId = UserContext.getUserId();
        User user = userConverter.fromUpdateRequest(updateRequest);
        user.setId(userId);
        user.setUpdateTime(LocalDateTime.now());
        log.info("用户更新个人信息:"+user.getId());
        userMapper.updateById(user);
        String key=REDIS_KEY_PREFIX_CACHE_USERINFO+userId;
        redisTemplate.delete(key);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public void logout(String accessToken) {
        Integer userId = jwtUtils.getUserIdFromToken(accessToken);
        Long remainTime =jwtUtils.getClaimsByToken(accessToken).getExpiration().getTime()-System.currentTimeMillis();
        if(remainTime>0){
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX_LOGIN_BLACKLIST+accessToken,"1",remainTime,TimeUnit.MILLISECONDS);

        }
        redisTemplate.delete("login:refresh_token:"+userId);
    }

    @Override
    public UserVO.TokenVo refreshToken(UserDTO.RefreshTokenDto refreshTokenDto) {
        Integer userId = jwtUtils.getUserIdFromToken(refreshTokenDto.getRefreshToken());
        String redisKey = REDIS_KEY_PREFIX_LOGIN_REFRESH_TOKEN + userId;
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在请重新登录");
        }
        String username = user.getUsername();
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if(storedToken == null || !storedToken.equals(refreshTokenDto.getRefreshToken())){
            throw new ServiceException("token已失效");
        }
        String accessToken = jwtUtils.generateAccessToken(userId,username);
        return new UserVO.TokenVo(accessToken);
    }
}
