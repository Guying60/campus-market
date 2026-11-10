package com.guying.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil; // 这里用了 Hutool 工具包，也可以换成 Jackson
import com.guying.pojo.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    // 线程池，用于逻辑过期的异步重建
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 方法一：存入Redis（带逻辑过期时间）
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        // 逻辑过期时间 = 当前时间 + 指定时间
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        
        // 存入 Redis (实际物理 TTL 设置为 -1 或很长，这里不设 TTL 让它永不过期，或者设个兜底长 TTL)
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 方法二：存入Redis（带普通 TTL）
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 核心方案 A：【逻辑过期】查询 (解决静态页击穿)
     * * @param keyPrefix Key前缀
     * @param id 查询ID
     * @param type 返回类型
     * @param dbFallback 数据库查询逻辑 (函数式编程)
     * @param time 逻辑过期时间长度
     * @param unit 时间单位
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {

        String key = keyPrefix + id;

        // 1. 从 Redis 查询
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 如果未命中，直接查库并写入
        if (StrUtil.isBlank(json)) {
            R r = dbFallback.apply(id);
            if (r == null) return null;
            this.setWithLogicalExpire(key, r, time, unit);
            return r;
        }

        // 3. 命中，反序列化 RedisData
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);

        // 【关键修复点】：这里 data 是 JSONObject，不能转 String
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);

        LocalDateTime expireTime = redisData.getExpireTime();

        // 4. 判断是否逻辑过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回
            return r;
        }

        // 5. 已过期，需要缓存重建
        String lockKey = "lock:" + keyPrefix + id;
        boolean isLock = tryLock(lockKey);

        if (isLock) {
            // 5.2 获取锁成功，开启独立线程去重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newR = dbFallback.apply(id);
                    // 重建缓存 (重置逻辑过期时间)
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }

        // 6. 返回旧数据
        return r;
    }

    /**
     * 核心方案 B：【互斥锁】查询 (解决价格击穿)
     */
    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {

        String key = keyPrefix + id;

        // 1. 从 Redis 查询
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 命中缓存，直接返回
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }

        // 判断命中是否为空值 (防止缓存穿透的空对象)
        if (json != null) {
            return null;
        }

        // 3. 缓存未命中，尝试获取互斥锁
        String lockKey = "lock:" + keyPrefix + id;
        R r = null;

        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                // 获取锁失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit); // 递归重试
            }

            // 4. 获取锁成功，再次检测缓存 (Double Check)
            String json2 = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json2)) {
                 return JSONUtil.toBean(json2, type);
            }

            // 5. 查数据库
            r = dbFallback.apply(id);

            // 6. 数据库也没数据，缓存空值 (防穿透)
            if (r == null) {
                stringRedisTemplate.opsForValue().set(key, "", 2L, TimeUnit.MINUTES);
                return null;
            }

            // 7. 写入缓存
            this.set(key, r, time, unit);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 8. 释放锁
            unlock(lockKey);
        }

        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}