package com.guying.repository;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisChatMemory implements ChatMemory {
    private RedisTemplate<String, List<Message>> redisTemplate;
    private static final String KEY_PREFIX = "chat:memory:";
    private static final long EXPIRE_TIME = 24;

    @Override
    public void add(String conversationId, Message message) {
        ChatMemory.super.add(conversationId, message);
    }

    @Override
    public void add(String s, List<Message> list) {

    }

    @Override
    public List<Message> get(String s) {
        return List.of();
    }

    @Override
    public void clear(String s) {

    }
}
