package com.wxl.agent.agent;

import com.wxl.agent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
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
                 @Qualifier("agentChatClient") ChatClient chatClient) {
        super(allTools);
        this.setName("Manus");
        String SYSTEM_PROMPT = """
                You are Manus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests,
                The output content needs to be in Chinese.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        /* 根据用户需求，主动选择最合适的工具或工具组合。对于复杂任务，您可以将问题分解，并逐步使用不同的工具来解决。
           使用每个工具后，请清晰地解释执行结果并提出后续步骤。如果您想在任何时候终止交互，
           请使用 `terminate` 工具/函数调用。 */
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
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