package com.wxl.agent.controller;

import cn.hutool.core.util.StrUtil;
import com.wxl.agent.common.BaseResponse;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.ResultUtils;
import com.wxl.agent.common.ThrowUtils;
import com.wxl.agent.model.dto.chatsession.ChatSessionDeleteRequest;
import com.wxl.agent.model.vo.ChatMessageVO;
import com.wxl.agent.model.vo.ChatSessionVO;
import com.wxl.agent.service.ChatSessionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
@Validated
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/list")
    public BaseResponse<List<ChatSessionVO>> listMySessions() {
        List<ChatSessionVO> list = chatSessionService.listMySessions();
        return ResultUtils.success(list);
    }

    /**
     * 获取指定会话的历史消息记录
     */
    @GetMapping("/history/{chatId}")
    public BaseResponse<List<ChatMessageVO>> getSessionHistory(
            @PathVariable
            @NotBlank(message = "chatId不能为空")
            String chatId) {
        ThrowUtils.throwIf(StrUtil.isBlank(chatId), ErrorCode.PARAMS_ERROR);
        List<ChatMessageVO> history = chatSessionService.getSessionHistory(chatId);
        return ResultUtils.success(history);
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSession(@Validated @RequestBody ChatSessionDeleteRequest request) {
        boolean result = chatSessionService.deleteSession(request);
        return ResultUtils.success(result);
    }
}