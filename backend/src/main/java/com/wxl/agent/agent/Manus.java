package com.wxl.agent.agent;

import com.wxl.agent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class Manus extends ToolCallAgent {

    public Manus(ToolCallback[] allTools,
                 @Qualifier("agentChatClient") ChatClient chatClient,
                 ChatMemory chatMemory) {
        super(allTools);
        this.setChatMemory(chatMemory);
        this.setName("Manus");
        String SYSTEM_PROMPT = """
                你是Manus，一款全能型AI助手，旨在解决用户提出的任意任务。
                你配备多款可用工具，能够调用工具高效完成复杂任务。
                回复内容优先使用中文。
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = """
                你是一个拥有复杂推理能力的超级智能体。针对用户的请求，请按以下严谨的规则工作：
                 1. 【思考与行动】：在调用任何工具之前，你必须先输出一段文字，向用户解释你当前的分析逻辑，以及接下来准备调用什么工具（这能让用户看到你的思考过程）。
                 2. 【工具调用】：解释完毕后，调用对应的工具获取信息。你可以将复杂问题拆解为多步来解决。
                 3. 【最终输出（极其重要）】：当你认为已经收集到了足够的背景信息，准备向用户交付最终的解答或方案时，请直接、立刻用详尽的纯文本输出完整的最终答案！
                 绝对不要再调用任何工具！绝对不要只输出“我现在为您输出计划”然后就停止！你必须在当前这段回复中直接把所有最终内容全部写完。
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
//        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultAdvisors(new MyLoggerAdvisor())
//                .build();
        this.setChatClient(chatClient);
    }
}