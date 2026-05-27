package com.wxl.agent.controller;

import com.wxl.agent.annotation.NotLogin;
import com.wxl.agent.common.BaseResponse;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.ResultUtils;
import com.wxl.agent.common.ThrowUtils;
import com.wxl.agent.converter.UserConverter; // 引入我们的装配器
import com.wxl.agent.model.dto.user.UserLoginRequest;
import com.wxl.agent.model.dto.user.UserRegisterRequest;
import com.wxl.agent.model.entity.User;
import com.wxl.agent.model.vo.LoginUserVO;
import com.wxl.agent.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/user")
@Validated  // 全局开启当前类的简单参数校验
public class UserController {

    private final UserService userService;
    private final UserConverter userConverter;

    // 构造函数注入
    public UserController(UserService userService, UserConverter userConverter) {
        this.userService = userService;
        this.userConverter = userConverter;
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    @NotLogin
    public BaseResponse<Long> userRegister(@Validated @RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long userId = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    @NotLogin
    public BaseResponse<LoginUserVO> userLogin(@Validated @RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() {
        // 1. 业务层内部获取完整的 User 实体（包含所有安全校验）
        User user = userService.getLoginUser();
        
        // 2. 使用专用的转换器装配成脱敏的 VO
        LoginUserVO loginUserVO = userConverter.toLoginUserVO(user);
        
        // 3. 安全地返回给前端
        return ResultUtils.success(loginUserVO);
    }
}

/**
 * 校验参数分两种情况：
 * 1.dto类中使用@NotBlank等注解，同时controller层对应的方法参数前加@Validated
 * 2.controller层方法简单参数中使用@NotBlank等注解，controller层这个类上要加@Validated
 */