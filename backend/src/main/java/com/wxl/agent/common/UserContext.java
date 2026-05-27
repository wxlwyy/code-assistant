package com.wxl.agent.common;

import com.wxl.agent.model.entity.User;

/**
 * 💡 用户上下文持有者（基于 ThreadLocal 实现线程隔离的安全存储）
 */
public class UserContext {
    
    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    /**
     * 保存当前登录用户
     */
    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户
     */
    public static User getUser() {
        return userThreadLocal.get();
    }

    /**
     * 清理当前登录用户（防止内存泄漏）
     */
    public static void clear() {
        userThreadLocal.remove();
    }
}