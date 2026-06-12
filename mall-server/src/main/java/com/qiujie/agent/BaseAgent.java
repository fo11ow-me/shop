package com.qiujie.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent 基类 — 状态机 + 步骤循环 + SSE 流式输出
 *
 * @author qiujie
 */
public abstract class BaseAgent {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected AgentState state = AgentState.IDLE;
    protected List<Map<String, String>> messageHistory = new ArrayList<>();
    protected int maxSteps = 10;
    protected SseEmitter emitter;

    /** 子类实现：执行一步推理 */
    protected abstract String step();

    /** 流式执行 agent，通过 SseEmitter 逐 step 推送结果 */
    public SseEmitter runStream(String userPrompt) {
        emitter = new SseEmitter(300_000L);
        state = AgentState.RUNNING;
        messageHistory.add(Map.of("role", "user", "content", userPrompt));

        new Thread(() -> {
            try {
                for (int i = 0; i < maxSteps && state == AgentState.RUNNING; i++) {
                    String result = step();
                    if (result != null && !result.isEmpty()) {
                        emitter.send(SseEmitter.event().data(result));
                    }
                    if (state == AgentState.FINISHED) break;
                }
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("Agent error", e);
                try {
                    emitter.send(SseEmitter.event().data("[ERROR] " + e.getMessage()));
                    emitter.complete();
                } catch (IOException ignored) {}
                state = AgentState.ERROR;
            }
        }).start();

        return emitter;
    }

    protected void finish() {
        state = AgentState.FINISHED;
    }
}
