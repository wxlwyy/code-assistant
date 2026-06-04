package com.wxl.agent.agent;

import cn.hutool.core.util.StrUtil;
import com.wxl.agent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            // 先思考（大模型没调用工具直接输出或调用工具获取程序的处理结果）
            boolean shouldAct = think();
            if (!shouldAct) {
                return "模型没有使用工具，直接给出了回答";
            }
            // 再行动（根据）
            return act();
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "步骤执行失败：" + e.getMessage();
        }
    }

    public Flux<String> runStream(String userPrompt, String chatId) {
        return Flux.<String>create(sink -> {

            // 核心修复：开启独立新线程！绝不能让大模型算力阻塞了 Spring 的网络发送流！
            new Thread(() -> {
                try {
                    if (getState() != AgentState.IDLE) {
                        sink.error(new RuntimeException("无法从" + getState() + "状态运行代理"));
                        return;
                    }
                    setState(AgentState.RUNNING);
                    getMessageList().add(new UserMessage(userPrompt));

                    // 准备用于存数据库的“思维链收集器”
                    sink.next("<think>\n");
                    StringBuilder thinkBuilder = new StringBuilder("<think>\n");
                    String finalAnswer = "任务执行结束，未获取到最终结果。";

                    // 开始多步思考循环
                    for (int i = 0; i < getMaxSteps() && getState() != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        setCurrentStep(stepNumber);

                        // 这一步是耗时的同步调用，因为在独立线程里，所以不会卡死网络
                        step();

                        // 获取当前这步的思考内容
                        String thinkingText = getCurrentStepThinking();

                        if (getState() == AgentState.FINISHED) {
                            // 把闭合标签也记录进要存数据库的 builder 中
                            thinkBuilder.append("</think>\n\n");

                            sink.next("</think>\n\n" + thinkingText);
                            finalAnswer = thinkingText;

                            // 加上 break，立刻硬性跳出 for 循环，绝对不允许死循环！
                            break;
                        } else {
                            thinkBuilder.append("Step ").append(stepNumber).append(": ").append(thinkingText).append("\n");
                            sink.next("Step " + stepNumber + ": " + thinkingText + "\n\n");
                        }
                    }

                    // 3. 兜底处理：如果达到最大步数被强制结束，也要闭合标签
                    if (getCurrentStep() >= getMaxSteps() && getState() != AgentState.FINISHED) {
                        setState(AgentState.FINISHED);
                        String endMsg = "执行结束：达到最大步骤";

                        // 这里只追加闭合标签即可！不要追加 endMsg，因为下面 dbContent 拼接时会加 finalAnswer
                        thinkBuilder.append("</think>\n\n");

                        sink.next("</think>\n\n" + endMsg);
                        finalAnswer = endMsg;
                    }

                    // 完美解决你的存储难题：循环彻底结束后，只存 2 条数据，严格保证顺序！
                    if (getChatMemory() != null && StrUtil.isNotBlank(chatId)) {
                        List<Message> finalMessagesToSave = new ArrayList<>();

                        // 第 1 条：先存用户提问
                        finalMessagesToSave.add(new UserMessage(userPrompt));

                        // 第 2 条：再存合成后的 AI 回答（包含灰色的折叠思考过程 + 最终文本）
                        String dbContent = thinkBuilder.toString() + finalAnswer;
                        finalMessagesToSave.add(new AssistantMessage(dbContent));

                        // 一次性顺序写入数据库
                        getChatMemory().add(chatId, finalMessagesToSave);
                    }

                    // 全部搞定，通知前端流结束
                    sink.complete();

                } catch (Exception e) {
                    log.error("Agent 运行异常", e);
                    sink.error(e);
                } finally {
                    cleanup();
                }
            }).start(); // 👈 别忘了启动线程！

        }, FluxSink.OverflowStrategy.LATEST);
    }

    /**
     * 获取模型的助手消息
     * @return
     */
    public String getCurrentStepThinking() {
        List<Message> msgs = getMessageList();
        // 从后往前找，找到最后一条有内容的助手消息
        for (int i = msgs.size() - 1; i >= 0; i--) {
            Message msg = msgs.get(i);
            if (msg instanceof AssistantMessage am) {

                // 💡 优先 1：如果大模型乖乖输出了思考文本，绝对优先用大模型的原话！这最自然！
                if (StrUtil.isNotBlank(am.getText())) {
                    return am.getText();
                }

                // 💡 优先 2：兜底方案。如果大模型偶尔哑巴了，只传了工具指令，我们就帮它翻译出来，防止前端卡顿空白
                if (am.hasToolCalls() && !am.getToolCalls().isEmpty()) {
                    String toolNames = am.getToolCalls().stream()
                            .map(AssistantMessage.ToolCall::name)
                            .collect(Collectors.joining(", "));
                    return "正在调用专属工具 [" + toolNames + "] 获取数据...";
                }
            }
        }
        return "正在整理思绪...";
    }

    // 在 ReActAgent 中添加
    /*public Flux<String> runStream(String userPrompt, String chatId) {
        return Flux.<String>create(sink -> {
                    // 1. 基础校验与初始化（与同步 run 方法相同）
                    if (getState() != AgentState.IDLE) {
                        sink.error(new RuntimeException("无法从" + getState() + "状态运行代理"));
                        return;
                    }
                    setState(AgentState.RUNNING);
                    getMessageList().add(new UserMessage(userPrompt));

                    // 2. 执行循环，每步结果实时推送
                    for (int i = 0; i < getMaxSteps() && getState() != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        setCurrentStep(stepNumber);
                        String stepResult = step(); // 复用 step()！
                        sink.next("Step " + stepNumber + ": " + getCurrentStepThinking()); // 实时推送
                    }

                    // 3. 处理完成与异常
                    if (getCurrentStep() >= getMaxSteps()) {
                        setState(AgentState.FINISHED);
                        sink.next("执行结束：达到最大步骤 (" + getMaxSteps() + ")");
                    }
                    sink.complete();
                }, FluxSink.OverflowStrategy.LATEST)
                .doFinally(signalType -> {
                    if (StrUtil.isNotBlank(chatId)) {
                        saveHistory(chatId);
                    }
                    cleanup();
                });
    }*/

}