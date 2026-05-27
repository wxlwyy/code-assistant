package com.wxl.agent.interceptor;

import cn.hutool.core.util.StrUtil;
import com.wxl.agent.annotation.NotLogin;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.UserContext;
import com.wxl.agent.exception.BusinessException;
import com.wxl.agent.model.entity.User;
import com.wxl.agent.service.UserService;
import com.wxl.agent.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    // 构造函数注入
    public JwtInterceptor(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 如果拦截到的不是 Controller 的方法（比如是静态资源文件），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 如果方法上，或者所在的 Controller 类上加了 @NotLogin 注解，直接放行！
        if (handlerMethod.hasMethodAnnotation(NotLogin.class) ||
                handlerMethod.getBeanType().isAnnotationPresent(NotLogin.class)) {
            return true;
        }

        // 1. 获取请求头
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isBlank(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        // 2. 截取并解密 Token
        String token = bearerToken.substring(7);
        User user = jwtUtils.getUserFromToken(token);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录凭证已失效");
        }

        // 3. 核心：将用户存入 ThreadLocal
        UserContext.setUser(user);

        // 4. 放行请求
        return true;
    }

    // afterCompletion这个方法的执行时机：此时请求结束响应已经发给前端了
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理 ThreadLocal 避免内存泄漏
        UserContext.clear();
    }
}

/**
 * 这三个参数的作用：
 *
 * - request：当前请求对象，用来拿请求头、请求参数、请求地址
 *
 * - response：当前响应对象，用来设置返回状态码、响应头
 *
 * - handler：Spring 内部用来表示“当前请求要执行的处理逻辑”的对象，核心就是你写的 Controller 方法
 *
 * if (!(handler instanceof HandlerMethod))
 *
 * 这行是拦截器的标准安全判断，企业开发几乎都会写。
 *
 * - handler 不只是 Controller 方法，也可能是：
 *
 * - 静态资源请求（比如访问图片、JS、CSS）
 *
 * - 错误页面请求
 *
 * - 其他框架内部的请求处理器
 *
 * - HandlerMethod 这个类型，专门代表“Controller 里的某个方法”
 *
 * - 所以这句代码的意思是：如果当前请求不是要执行 Controller 方法，直接放行，不做任何拦截逻辑
 *
 * 如果不写这句，遇到静态资源请求时，把 handler 强转成 HandlerMethod 会直接报错，导致页面加载失败。
 *
 * handlerMethod.hasMethodAnnotation(NotLogin.class) || handlerMethod.getBeanType().isAnnotationPresent(NotLogin.class)
 *
 * 是自定义注解配合的免登判断，也是企业里常用的写法。
 *
 * 这行是同时判断两种情况：
 *
 * 1.handlerMethod.hasMethodAnnotation(NotLogin.class)
 * 判断当前Controller方法上有没有加 @NotLogin 注解
 *
 * 2.handlerMethod.getBeanType().isAnnotationPresent(NotLogin.class)
 * 判断当前Controller类上有没有加 @NotLogin 注解
 *
 * 只要方法或者类上有一个标记了免登注解，就直接放行，不做 Token 校验。
 */