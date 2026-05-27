package com.wxl.agent.service;

import com.wxl.agent.model.dto.user.UserLoginRequest;
import com.wxl.agent.model.dto.user.UserRegisterRequest;
import com.wxl.agent.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxl.agent.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author wxl
* @description 针对表【tb_user(用户表)】的数据库操作Service
* @createDate 2026-05-24 17:19:44
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册请求参数
     * @return 新创建的用户 ID
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录请求参数
     * @return 登录成功后的视图对象（包含 Token）
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest);

    /**
     * 获取当前登录用户（从 JWT Token 中解析并查询）
     * 这个方法两个用途：第一是返回的User供内部使用，第二是controller层脱敏后给前端使用
     *
     * @return 当前登录的用户实体
     */
    User getLoginUser();
}
