package com.wxl.agent.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 历史会话视图对象 (给前端侧边栏展示用)
 */
@Data
public class ChatSessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID (即 chatId)
     */
    private String id;

    /**
     * 会话标题 (通常是用户的第一句话)
     */
    private String title;

    /**
     * 智能体类型 (STANDARD / REASONING)
     */
    private String agentType;

    /**
     * 创建时间
     */
    private Date createTime;
}