package com.wxl.agent.aop;

import cn.hutool.core.util.StrUtil;
import com.wxl.agent.annotation.AuthCheck;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.UserContext;
import com.wxl.agent.exception.BusinessException;
import com.wxl.agent.model.dto.enums.UserRoleEnum;
import com.wxl.agent.model.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限校验 AOP 切面
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 执行拦截
     * @param joinPoint 切入点
     * @param authCheck 拦截的注解
     * @return 代理执行结果
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        
        // 1. 如果这个接口不需要任何特定的角色，直接放行
        if (StrUtil.isBlank(mustRole)) {
            return joinPoint.proceed();
        }

        // 2. 直接从我们之前写好的 UserContext 里拿到当前登录用户！
        // （因为此时 JWT 拦截器已经执行完毕了，所以上下文中绝对有值）
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 3. 提取当前用户的角色
        String userRole = currentUser.getUserRole();

        // 4. 如果该方法需要的角色与当前用户角色不符就拦截
        if (!mustRole.equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无访问权限");
        }

        // 放行，执行原本的方法
        return joinPoint.proceed();
    }
}