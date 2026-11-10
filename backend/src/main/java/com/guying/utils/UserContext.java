package com.guying.utils;

public class UserContext {
    private static final ThreadLocal<Integer> userThreadLocal = new ThreadLocal<>();

    public static void setUserId(Integer id) {
        userThreadLocal.set(id);
    }

    public static Integer getUserId() {
        return userThreadLocal.get();
    }

    public static void remove() {
        userThreadLocal.remove();
    }
}
