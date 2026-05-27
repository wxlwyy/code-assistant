package com.wxl.agent.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 已登录用户视图对象（过滤敏感信息）
 */
@Data
public class LoginUserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userAccount;
    private String userName;
    private String userAvatar;
    private String userRole;
    private String token; //  关键：在用户登录后把 JWT Token 一起返回给前端
    private LocalDateTime createTime;
}