package com.qiujie.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.qiujie.agent.MallCustomerAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 智能客服 AI 控制器 — SSE 流式响应 + 多轮记忆
 *
 * @author qiujie
 */
@RestController
@RequestMapping("/portal/ai")
@Tag(name = "门户端-智能客服")
public class AiController {

    private final MallCustomerAgent agent;

    public AiController(MallCustomerAgent agent) {
        this.agent = agent;
    }

    @Operation(summary = "智能客服对话（SSE 流式）")
    @PostMapping("/chat")
    public SseEmitter chat(@RequestParam String message,
                           @RequestParam(defaultValue = "default") String chatId) {
        Integer userId = null;
        try {
            userId = StpUtil.getLoginIdAsInt();
        } catch (Exception ignored) {
            // 未登录也允许使用
        }
        return agent.chatStream(chatId, message, userId);
    }

    @Operation(summary = "清除会话历史")
    @DeleteMapping("/chat/{chatId}")
    public Map<String, Object> clearSession(@PathVariable String chatId) {
        agent.clearSession(chatId);
        return Map.of("ok", true);
    }
}
