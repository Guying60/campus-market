package com.guying.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 作用：生成Token、解析Token、获取用户信息
 */
@Component
public class JwtUtils {

    public static final long ACCESS_TOKEN_EXPIRE = 1000 * 60 * 15;

    public static final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 7;

    // 从 application.yml 读取配置
    @Value("${jwt.secret}")
    private String secret;


    /**
     * 生成 JWT Token
     * @param userId 用户ID
     * @param username 用户名 (或昵称)
     * @return String token字符串
     */
    public String generateAccessToken(Integer userId, String username) {
        // 1. 定义载荷 (Payload): 存放在 Token 里的公开信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 2. 生成 Key
        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        // 3. 构建 Token
        return Jwts.builder()
                .setClaims(claims) // 设置自定义信息
                .setSubject(username) // 设置主题(通常是用户名)
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE)) // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    public String createRefreshToken(Integer userId) {
        // 1. 定义载荷 (Payload): 存放在 Token 里的公开信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        // 2. 生成 Key
        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        // 3. 构建 Token
        return Jwts.builder()
                .setClaims(claims) // 设置自定义信息
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE)) // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    /**
     * 解析 Token 获取 Claims (载荷)
     * 如果 Token 过期或被篡改，这里会抛出异常
     */
    public  Claims getClaimsByToken(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 Token 是否过期
     * @return true=已过期, false=没过期
     */
    public  boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getClaimsByToken(token).getExpiration();
            return expirationDate.before(new Date());
        } catch (Exception e) {
            // 如果解析失败，也视为过期或无效
            return true;
        }
    }

    /**
     * 辅助方法：从 Token 中直接获取 User ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsByToken(token);
        return Integer.parseInt(claims.get("userId").toString());
    }
}