package com.wxl.agent.constant;

/**
 * 角色常量
 */
public final class UserRoleConstant {
    // 私有构造：禁止实例化（因为new出对象调用属性浪费内存没意义）
    private UserRoleConstant() {}

    /** 普通用户角色 */
    public static final String USER = "user";

    /** 管理员角色 */
    public static final String ADMIN = "admin";
}