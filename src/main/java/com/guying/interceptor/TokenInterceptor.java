package com.guying.interceptor;

import com.guying.exception.JwtException;
import com.guying.utils.JwtUtils;
import com.guying.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.guying.common.constants.RedisConstants.REDIS_KEY_PREFIX_LOGIN_BLACKLIST;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //放行浏览器的侦察兵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new JwtException("token不存在");
        }

        if (jwtUtils.isTokenExpired(token)) {
            throw new JwtException("token已过期");
        }
        Boolean isBlacklisted = redisTemplate.hasKey(REDIS_KEY_PREFIX_LOGIN_BLACKLIST+token);
        if(isBlacklisted){
            throw new JwtException("token处在黑名单");
        }
        log.info("Token 校验通过，放行访问: {}", token);
        Integer userId = jwtUtils.getUserIdFromToken(token);
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("请求结束，释放资源");
        UserContext.remove();
    }
}
