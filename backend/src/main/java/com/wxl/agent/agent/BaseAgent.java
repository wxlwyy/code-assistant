package com.wxl.agent.agent;

import cn.hutool.core.util.StrUtil;
import com.wxl.agent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性（在Manus类中手动传入一个名字）
    private String name;

    // 系统提示词（主要给ai说明了一下能调用多种工具）
    private String systemPrompt;

    /* 对于复杂任务，可以拆解问题，逐步使用不同的工具，每次使用工具后说明结果，
    并给出下一步建议，如果任务解决了就调用终止工具（将执行状态改为完成） */
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 20;

    // 对话客户端（设置大模型、提示词、拦截器等信息）
    private ChatClient chatClient;

    // 用户、系统、助手、工具响应Message列表，给大模型当会话上下文
    private List<Message> messageList = new ArrayList<>();

    // 工具执行结果列表
    List<String> results = new ArrayList<>();

    private String chatId;

    private ChatMemory chatMemory;

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1、基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2、执行，更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
//        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep == maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 3、清理资源
            this.cleanup();
        }
    }

    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }

    public void saveHistory(String chatId) {
        if (chatMemory == null || StrUtil.isBlank(chatId)) {
            return;
        }
        // 1. 清空该 conversation 的旧记录（可选，如果希望覆盖）
        // 需要注入 JdbcTemplate，执行 DELETE FROM chat_memory WHERE conversation_id = ?

        // 2. 从 messageList 中筛选需要保留的消息
        List<Message> historyMessages = new ArrayList<>();
        for (Message msg : messageList) {
            if (msg instanceof UserMessage userMsg) {
                // 只保留第一条用户消息（原始提问）
                // 或者更健壮：排除内容与 nextStepPrompt 完全相同的消息
                if (!StrUtil.equals(userMsg.getText(), getNextStepPrompt())) {
                    historyMessages.add(msg);
                }
            } else if (msg instanceof AssistantMessage am && StrUtil.isNotBlank(am.getText())) {
                // 只保留有文本内容的助手消息
                historyMessages.add(msg);
            }
            // 忽略 ToolResponseMessage，因为用户通常不需要看原始JSON结果
        }

        // 3. 一次性写入多条数据
        chatMemory.add(chatId, historyMessages);
    }
}