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

import java.util.List;

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

    // 在 ReActAgent 中添加
    public Flux<String> runStream(String userPrompt) {
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
                .doFinally(signalType -> cleanup());
    }

    /**
     * 获取模型的助手消息
     * @return
     */
    public String getCurrentStepThinking() {
        List<Message> msgs = getMessageList();
        // 从后往前找，找到最后一条有文本内容的 AssistantMessage
        for (int i = msgs.size() - 1; i >= 0; i--) {
            Message msg = msgs.get(i);
            if (msg instanceof AssistantMessage am && StrUtil.isNotBlank(am.getText())) {
                return am.getText();
            }
        }
        return "正在思考...";
    }

}