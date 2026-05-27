package com.wxl.agent.controller;

import com.wxl.agent.agent.Manus;
import com.wxl.agent.app.LoveApp;
import com.wxl.agent.common.BaseResponse;
import com.wxl.agent.common.ResultUtils;
import com.wxl.agent.model.dto.ai.AiChatRequest;
import com.wxl.agent.service.ChatSessionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

    private final ChatSessionService chatSessionService;

    public AiController(LoveApp loveApp,
                        ToolCallback[] allTools,
                        @Qualifier("agentChatClient") ChatClient chatClient,
                        ChatSessionService chatSessionService) {
        this.loveApp = loveApp;
        this.allTools = allTools;
        this.chatClient = chatClient;
        this.chatSessionService = chatSessionService;
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     * 第一种：Flux<String>（标准写法，最常用）
     * 原理：设置响应类型为 text/event-stream，告诉浏览器“我将持续发送 SSE 事件流”。方法返回 Flux<String>，
     * Spring WebFlux 会自动把每个字符串包装成 SSE 事件的 data 字段，发送给客户端。
     * 适用场景：90% 的流式传输场景。前端只需标准 EventSource API 就能消费。
     * @param aiChatRequest
     * @return
     */
    @PostMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(@Validated @RequestBody AiChatRequest aiChatRequest) {
        String chatId = aiChatRequest.getChatId();
        String message = aiChatRequest.getMessage();
        chatSessionService.initSessionIfAbsent(chatId, message, "STANDARD");

        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 流式调用 Manus 超级智能体
     * Validated：Spring 提供的注解，加在类上，告诉 Spring “这个类的方法参数需要校验”。
     * NotBlank：一个约束注解，表示字符串不能为 null，且长度必须大于 0，且不能全是空白字符。
     *
     * @param aiChatRequest
     * @return
     */
    @PostMapping("/manus/chat/sse")
    public Flux<String> doChatWithManusSSE(@Validated @RequestBody AiChatRequest aiChatRequest) {
        String chatId = aiChatRequest.getChatId();
        String message = aiChatRequest.getMessage();
        chatSessionService.initSessionIfAbsent(chatId, message, "REASONING");

        Manus manus = new Manus(allTools, chatClient);
        return manus.runStream(message);
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
     * 第二种：Flux<ServerSentEvent<String>>（精细控制，偶尔用）
     * 区别：把每个字符串手动包装成 ServerSentEvent 对象。你可以在此基础上，给每个事件添加 id、event（事件类型）、
     * retry（重连时间）、comment（注释）等 SSE 协议支持的字段。什么时候用到：前端需要区分不同事件类型
     * （如 event:thinking vs event:result），或者需要断线重连、事件 ID 去重时。日常开发很少用到。
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        Flux<ServerSentEvent<String>> map = loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
        return map;
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
}

/**
 * 分清两种传参方式
 *
 * 1. 原来的 GET 写法
 *
 * GET 请求参数默认放在URL地址栏，对应注解：@RequestParam
 *
 * - 单个/多个零散参数（message、chatId），不用封装对象
 * - 代码形态：
 *
 * java
 *
 * @GetMapping("/chat/sse")
 * public Flux<String> chatSse(
 *     String message,
 *     String chatId
 * ) {
 *     // 底层等价于自动加了 @RequestParam
 * }
 *
 *
 * 浏览器/EventSource 调用时，地址长这样：
 * /chat/sse?message=你好&chatId=abc123
 *
 * 2. 改成 POST + SSE 流式
 *
 * POST 传复杂参数，规范做法是把参数放到JSON 请求体，对应注解：@RequestBody
 *
 * - 零散参数必须封装成一个 DTO 对象
 * - 接口必须加 @PostMapping + @RequestBody + 开启校验 @Validated
 * - 响应头指定流式格式 produces = MediaType.TEXT_EVENT_STREAM_VALUE
 *
 *
 *
 * 1. GET 为什么不用写 @RequestParam？
 *
 * Spring MVC 规则：
 *
 * - GET 请求、普通零散参数，可以省略 @RequestParam，框架自动识别；
 * - 本质还是请求参数绑定，数据来自 URL。
 *
 * 2. 改成 POST 后，能不能继续用零散参数？
 *
 * 技术上可以（用 @RequestParam 拼在 URL），但不推荐、不符合企业规范：
 *
 * - 违背 POST 语义；
 * - 长文本、敏感内容依然暴露在 URL；
 * - 既然改成 POST，就统一用 JSON 请求体 + DTO + @RequestBody。
 *
 * 3. 为什么一定要封装 DTO？
 *
 * @RequestBody 规则：只能接收一个对象，不能直接接收多个零散字符串参数。
 * 所以必须把 message、chatId 打包成一个实体/DTO。
 */