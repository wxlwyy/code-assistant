package com.wxl.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 💡 标有此注解的接口，无需校验 Token 登录状态即可直接访问
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 可以加在方法上，也可以加在整个 Controller 类上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface NotLogin {
}