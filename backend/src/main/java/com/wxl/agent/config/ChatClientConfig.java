package com.wxl.agent.config;

import com.wxl.agent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    public static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 恋爱助手专用的 ChatClient
     * 绑定了固定人设和对话记忆
     */
    @Bean("loveAppChatClient")
    public ChatClient loveAppChatClient(ChatMemoryRepository jdbcChatMemoryRepository,
                                   ChatModel chatModel) {  //dashscopeChatModel
        //        // 初始化基于文件的对话记忆
//        val inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)  // 能记住5轮对话（因为1轮对话是2个消息）
                .build();
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 智能体专用的 ChatClient
     * 只配置日志，不绑定固定人设（人设由智能体动态传入）
     */
    @Bean("agentChatClient") // ← 显式命名为 agentChatClient
    public ChatClient agentChatClient(ChatModel dashscopeChatModel) {
        return ChatClient.builder(dashscopeChatModel)
//                .defaultAdvisors(new MyLoggerAdvisor()) // 只配日志拦截器
                // 注意：没有 .defaultSystem()，人设由 ToolCallAgent.think() 动态传入
                .build();
    }
}