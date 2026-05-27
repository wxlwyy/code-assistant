package com.wxl.agent.controller;

import com.wxl.agent.common.BaseResponse;
import com.wxl.agent.common.ErrorCode;
import com.wxl.agent.common.ResultUtils;
import com.wxl.agent.common.ThrowUtils;
import com.wxl.agent.model.dto.chatsession.ChatSessionDeleteRequest;
import com.wxl.agent.model.vo.ChatSessionVO;
import com.wxl.agent.service.ChatSessionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
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
     * 删除会话
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSession(@Validated @RequestBody ChatSessionDeleteRequest request) {
        boolean result = chatSessionService.deleteSession(request);
        return ResultUtils.success(result);
    }
}