package com.guying.repository;

import com.guying.utils.OrderUtils;
import com.guying.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class InRedisChatHistoryRepository implements ChatHistoryRepository {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    /**
     * 保存会话记录
     * @param
     * @param
     */
    public void save(String chatId) {
        Integer userId = UserContext.getUserId();
        String key = "chatHistory:" + userId + ":" + chatId;
        stringRedisTemplate.opsForValue().set(key, chatId, 24, TimeUnit.HOURS);
    }


    @Override
    /**
     * 获取会话记录
     * @param
     * @param
     * @return
     */
    public List<String> get() {
        Integer userId = UserContext.getUserId();
        Set<String> keys = stringRedisTemplate.keys("chatHistory:" + userId + ":*");
        if (keys == null){
            return List.of();
        }
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 删除会话记录
     * @param chatId
     */
    @Override
    public void delete(String chatId) {
        Integer userId = UserContext.getUserId();
        stringRedisTemplate.delete("chatHistory:" + userId + ":" + chatId);
    }
}
