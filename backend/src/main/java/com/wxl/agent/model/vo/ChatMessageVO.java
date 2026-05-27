package com.wxl.agent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 消息详情视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 发送者角色："user" (用户) 或 "assistant" (AI)
     */
    private String type;

    /**
     * 消息的具体文本内容
     */
    private String content;
}