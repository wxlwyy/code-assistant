package com.wxl.agent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.ThrowUtils;
import com.wxl.agent.common.UserContext;
import com.wxl.agent.converter.UserConverter;
import com.wxl.agent.exception.BusinessException;
import com.wxl.agent.model.dto.user.UserLoginRequest;
import com.wxl.agent.model.dto.user.UserRegisterRequest;
import com.wxl.agent.model.entity.User;
import com.wxl.agent.model.vo.LoginUserVO;
import com.wxl.agent.service.UserService;
import com.wxl.agent.mapper.UserMapper;
import com.wxl.agent.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
* @author wxl
* @description 针对表【tb_user(用户表)】的数据库操作Service实现
* @createDate 2026-05-24 17:19:44
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private final JwtUtils jwtUtils;

    private final UserConverter userConverter;

    public UserServiceImpl(JwtUtils jwtUtils, UserConverter userConverter) {
        this.jwtUtils = jwtUtils;
        this.userConverter = userConverter;
    }

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        // 1. 业务逻辑校验
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 2. 检查账号是否已经存在（使用 MP 提供的方法）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0,
                ErrorCode.PARAMS_ERROR, "账号已存在");

        // 3. 密码加密
        // 盐值混淆，防止被彩虹表破译
        String encryptPassword = BCrypt.hashpw(userPassword, BCrypt.gensalt());

        // 4. 插入新用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("AI用户_" + RandomUtil.randomNumbers(4)); // 默认随机昵称
        user.setUserRole("user"); // 默认角色

        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult,
                ErrorCode.PARAMS_ERROR, "注册失败，数据库异常");

        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 1. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        User user = this.getOne(queryWrapper);

        // 行业安全规范：不要具体说“账号不存在”还是“密码错误”，统一返回“账号或密码错误”，防止黑客暴力破解账号
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号或密码错误");

        // 2. 校验密码是否匹配
        // BCrypt.checkpw 会自动解析数据库中的 user.getUserPassword() 提取里面的盐，然后和明文 userPassword 运算比对
        boolean isPasswordMatch = BCrypt.checkpw(userPassword, user.getUserPassword());
        ThrowUtils.throwIf(!isPasswordMatch, ErrorCode.PARAMS_ERROR, "账号或密码错误");

        // 3. 登录成功，生成 JWT Token
        String token = jwtUtils.createToken(user);

        // 4. 脱敏
        LoginUserVO loginUserVO = userConverter.toLoginUserVO(user);
        loginUserVO.setToken(token); // 塞入 Token

        return loginUserVO;
    }

    @Override
    public User getLoginUser() {
        User user = UserContext.getUser();
        Long userId = user.getId();

        // 从数据库查询最新的用户信息
        user = this.getById(userId);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在或已被封禁");
        }

        return user;
    }
}




