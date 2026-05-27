package com.wxl.agent.model.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AiChatRequest implements Serializable {

    private static final long serialVersionUID = 1376370062944190526L;

    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    @NotBlank(message = "消息不能为空")
    private String message;
}
