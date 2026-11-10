package com.guying.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    /**
     * 注册或修改密码时调用：生成哈希
     * BCrypt 会自动处理 Salt，并将其包含在最终的字符串中
     */
    public static String hashPassword(String plainPassword) {
        // gensalt() 默认负载因子是 10，适合现在的硬件
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 登录时调用：验证密码
     * @param plainPassword 用户输入的明文密码
     * @param hashedPassword 数据库中取出的哈希值
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // 记录日志，防止格式错误的哈希值导致系统异常
            return false;
        }
    }

//    public static void main(String[] args) {
//        String originalPassword = "MySecretPassword123";
//
//        // 1. 模拟注册：加密
//        String storedHash = hashPassword(originalPassword);
//        System.out.println("存入数据库的哈希值: " + storedHash);
//        // 输出示例: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWrn9IzymGNJ.. (包含了算法标识、强度、盐和哈希)
//
//        // 2. 模拟登录：验证
//        boolean isMatch = checkPassword("MySecretPassword123", storedHash);
//        System.out.println("密码验证结果: " + isMatch); // true
//
//        boolean isFake = checkPassword("WrongPassword", storedHash);
//        System.out.println("错误密码验证结果: " + isFake); // false
//    }
}