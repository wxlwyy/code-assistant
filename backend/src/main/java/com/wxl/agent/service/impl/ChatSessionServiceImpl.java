package com.wxl.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.ThrowUtils;
import com.wxl.agent.common.UserContext;
import com.wxl.agent.converter.ChatSessionConverter;
import com.wxl.agent.exception.BusinessException;
import com.wxl.agent.model.dto.chatsession.ChatSessionDeleteRequest;
import com.wxl.agent.model.entity.ChatSession;
import com.wxl.agent.model.vo.ChatMessageVO;
import com.wxl.agent.model.vo.ChatSessionVO;
import com.wxl.agent.service.ChatSessionService;
import com.wxl.agent.mapper.ChatSessionMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author wxl
* @description 针对表【tb_chat_session(会话映射表)】的数据库操作Service实现
* @createDate 2026-05-24 17:32:57
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{

    private final ChatSessionConverter chatSessionConverter;

    private final ChatMemory chatMemory;

    public ChatSessionServiceImpl(ChatSessionConverter chatSessionConverter, ChatMemory chatMemory) {
        this.chatSessionConverter = chatSessionConverter;
        this.chatMemory = chatMemory;
    }

    /**
     * 获取当前登录用户的历史会话列表
     */
    @Override
    public List<ChatSessionVO> listMySessions() {
        // 1. 无感获取当前登录用户的 ID
        Long userId = UserContext.getUser().getId();

        // 2. 构造查询条件：查自己的，按创建时间倒序排（最新的在最上面）
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");

        // 3. 从数据库查出实体列表
        List<ChatSession> sessionList = this.list(queryWrapper);

        // 4. 用 MapStruct 高效转换为 VO 列表返回
        return chatSessionConverter.toVoList(sessionList);
    }

    /**
     * 删除会话（带防越权校验）
     */
    @Override
    public boolean deleteSession(ChatSessionDeleteRequest request) {
        Long userId = UserContext.getUser().getId();
        String sessionId = request.getId();

        // 1. 先查出来确认存在
        ChatSession chatSession = this.getById(sessionId);
        ThrowUtils.throwIf(chatSession == null,
                ErrorCode.NOT_FOUND_ERROR, "会话不存在");

        // 2. 防止越权，判断这个会话是不是当前用户的！
        ThrowUtils.throwIf(!chatSession.getUserId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "无权删除他人的会话");

        // 删除spring ai维护的chat memory的相关数据
        chatMemory.clear(sessionId);

        // 3. 执行删除
        return this.removeById(sessionId);
    }

    @Override
    public void initSessionIfAbsent(String chatId, String message, String agentType) {
        // 1. 拿着 chatId 去查会话是否存在
        ChatSession session = this.getById(chatId);

        // 2. 如果不存在，执行“懒创建”
        if (session == null) {
            session = new ChatSession();
            session.setId(chatId);
            session.setUserId(UserContext.getUser().getId()); // 绑定当前登录用户的 ID
            session.setAgentType(agentType);

            // 提取前 15 个字作为标题
            String title = message;
            if (message.length() > 15) {
                title = message.substring(0, 15) + "...";
            }
            session.setTitle(title);

            // 3. 写入数据库
            boolean saved = this.save(session);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "新建会话失败");
        }
    }

    @Override
    public List<ChatMessageVO> getSessionHistory(String chatId) {
        Long userId = UserContext.getUser().getId();

        // 1. 安全控制：先验证这个会话是不是当前登录用户的，防止黑客水平越权偷看别人的聊天记录！
        ChatSession session = this.getById(chatId);
        ThrowUtils.throwIf(session == null, ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        ThrowUtils.throwIf(!session.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权访问此会话");

        // 2. 直接调用 Spring AI 的 get 接口，获取消息
        List<Message> messages = chatMemory.get(chatId);

        // 3. 过滤并装配成给前端的统一 VO 格式
        return messages.stream()
                // 过滤掉系统消息（SYSTEM）和工具消息（TOOL），这些不需要展示给前端用户看
                .filter(msg -> msg.getMessageType() == MessageType.USER || msg.getMessageType() == MessageType.ASSISTANT)
                // 转换为 ChatMessageVO 对象
                .map(msg -> new ChatMessageVO(
                        msg.getMessageType().name().toLowerCase(), // 转为小写 "user" 或 "assistant"
                        msg.getText()
                ))
                .collect(Collectors.toList());
    }
}



