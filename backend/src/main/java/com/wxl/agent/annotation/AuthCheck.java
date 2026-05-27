package com.wxl.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 */
@Target(ElementType.METHOD) // 只能打在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    
    /**
     * 必须具有某个角色才能访问
     */
    String mustRole() default "";
}