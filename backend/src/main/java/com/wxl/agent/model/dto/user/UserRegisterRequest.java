package com.wxl.agent.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 16, message = "账号长度必须在 4 到 16 位之间")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6 到 20 位之间")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;
}