package com.wxl.agent.service;

import com.wxl.agent.model.dto.chatsession.ChatSessionDeleteRequest;
import com.wxl.agent.model.entity.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxl.agent.model.vo.ChatMessageVO;
import com.wxl.agent.model.vo.ChatSessionVO;

import java.util.List;

/**
* @author wxl
* @description 针对表【tb_chat_session(会话映射表)】的数据库操作Service
* @createDate 2026-05-24 17:32:57
*/
public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 查看会话列表
     * @return
     */
    List<ChatSessionVO> listMySessions();

    /**
     * 删除某个会话
     * @param request
     * @return
     */
    boolean deleteSession(ChatSessionDeleteRequest request);

    /**
     * 💡 初始化会话（如果会话不存在，则自动进行懒创建并保存）
     *
     * @param chatId 会话 ID
     * @param message 用户发出的第一句话
     * @param agentType 智能体类型
     */
    void initSessionIfAbsent(String chatId, String message, String agentType);

    /**
     * 获取某个会话的历史消息记录
     */
    List<ChatMessageVO> getSessionHistory(String chatId);
}
