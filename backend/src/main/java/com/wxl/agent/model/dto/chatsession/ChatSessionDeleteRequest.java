package com.wxl.agent.model.dto.chatsession;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSessionDeleteRequest implements Serializable {

    /**
     * id
     */
    @NotBlank(message = "会话 ID 不能为空")
    private String id;

    private static final long serialVersionUID = 3372433899541668240L;
}