package com.wxl.agent.controller;

import com.wxl.agent.agent.Manus;
import com.wxl.agent.app.LoveApp;
import com.wxl.agent.common.BaseResponse;
import com.wxl.agent.common.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
@Validated
public class AiController {

    private final LoveApp loveApp;

    private final ToolCallback[] allTools;

    private final ChatClient chatClient;

    public AiController(LoveApp loveAp,
                        ToolCallback[] allTools,
                        @Qualifier("agentChatClient") ChatClient chatClient) {
        this.loveApp = loveAp;
        this.allTools = allTools;
        this.chatClient = chatClient;
    }

    /**
     * 同步调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> doChatWithLoveAppSync(String message, String chatId) {
        String content = loveApp.doChat(message, chatId);
        return ResultUtils.success(content);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     * 第一种：Flux<String>（标准写法，最常用）
     * 原理：设置响应类型为 text/event-stream，告诉浏览器“我将持续发送 SSE 事件流”。方法返回 Flux<String>，
     * Spring WebFlux 会自动把每个字符串包装成 SSE 事件的 data 字段，发送给客户端。
     * 适用场景：90% 的流式传输场景。前端只需标准 EventSource API 就能消费。
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public BaseResponse<Flux<String>> doChatWithLoveAppSSE(String message, String chatId) {
        Flux<String> stringFlux = loveApp.doChatByStream(message, chatId);
        return ResultUtils.success(stringFlux);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     * 第二种：Flux<ServerSentEvent<String>>（精细控制，偶尔用）
     * 区别：把每个字符串手动包装成 ServerSentEvent 对象。你可以在此基础上，给每个事件添加 id、event（事件类型）、
     * retry（重连时间）、comment（注释）等 SSE 协议支持的字段。什么时候用到：前端需要区分不同事件类型
     * （如 event:thinking vs event:result），或者需要断线重连、事件 ID 去重时。日常开发很少用到。
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public BaseResponse<Flux<ServerSentEvent<String>>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        Flux<ServerSentEvent<String>> map = loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
        return ResultUtils.success(map);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     * 第三种：SseEmitter（兼容老版本，已过时）
     * 原理：SseEmitter 是 Spring MVC（非 WebFlux）提供的异步响应机制。它返回一个 SseEmitter 对象，
     * 然后通过 subscribe 手动监听流，把每个数据块 send 出去。
     * 为什么现在很少用：它是 Spring 5 为了在老式 Servlet 容器（Tomcat）中实现 SSE 而引入的。
     * 你现在使用的是 WebFlux，Flux<String> 就是它的原生、一等公民支持。SseEmitter 对 WebFlux 项目来说，完全多此一举。
     * @param message
     * @param chatId
     * @return
     */
//    @GetMapping(value = "/love_app/chat/sse_emitter")
//    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
//        // 创建一个超时时间较长的 SseEmitter
//        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
//        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
//        loveApp.doChatByStream(message, chatId)
//                .subscribe(chunk -> {
//                    try {
//                        sseEmitter.send(chunk);
//                    } catch (IOException e) {
//                        sseEmitter.completeWithError(e);
//                    }
//                }, sseEmitter::completeWithError, sseEmitter::complete);
//        // 返回
//        return sseEmitter;
//    }

    /**
     * 流式调用 Manus 超级智能体
     * Validated：Spring 提供的注解，加在类上，告诉 Spring “这个类的方法参数需要校验”。
     * NotBlank：一个约束注解，表示字符串不能为 null，且长度必须大于 0，且不能全是空白字符。
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat/sse")
    public BaseResponse<Flux<String>> doChatWithManusSSE(
            @NotBlank(message = "用户提示词不能为空") String message) { // 参数上加约束注解
        Manus manus = new Manus(allTools, chatClient);
        Flux<String> stringFlux = manus.runStream(message);
        return ResultUtils.success(stringFlux);
    }
}