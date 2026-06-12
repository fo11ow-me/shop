package com.qiujie.agent;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qiujie.agent.tools.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 商城智能客服 Agent — 多轮记忆 + Query 改写 + 结构化输出
 *
 * @author qiujie
 */
@Component
public class MallCustomerAgent {

    private static final int MAX_HISTORY = 20;

    private static final String SYSTEM_PROMPT = """
            你是"mall商城"的智能客服助手。请用中文、友好、简洁地回答用户问题。

            你的能力：
            1. 搜索商品 — 当用户想找某类商品时使用 searchProducts
            2. 查看商品详情 — 当用户询问具体商品信息时使用 getProductDetail
            3. 查询订单 — 当用户想查自己的订单时使用 queryOrders
            4. 热销排行 — 当用户问"热卖""热门""推荐"时使用 getHotProducts
            5. 知识库搜索 — 当用户询问退货、换货、退款、配送、支付、发票、保修等规则时使用 searchKnowledge
               ⚠️ 调用 searchKnowledge 时，query 参数必须改写为正式的关键词组合，不要直接用口语。
               例如："能退吗" → "退货政策 退款条件"；"几天到" → "配送时效"。

            规则：
            - 优先使用工具获取实时数据，不要编造信息
            - 回答格式要求（严格遵守）：
              【问题分类】一句话总结用户问题类型，如"商品咨询""售后政策""订单查询"等
              【简短回复】100字以内的核心答案
              【补充说明】必要时补充1条关键细节，不需要则省略
            - 如果用户的问题与购物无关，友好引导回商城功能
            - 用户未登录时无法查订单，请提示登录
            - 回答中不要使用任何 Markdown 格式符号（*、#、` 等）
            """;

    private final ToolCallAgent toolCallAgent;

    /** 会话历史缓存：30 分钟无活动自动清除 */
    private final Cache<String, List<Map<String, String>>> sessions = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public MallCustomerAgent(ToolCallAgent toolCallAgent, SearchProductsTool searchProductsTool,
                             GetProductDetailTool getProductDetailTool, QueryOrderTool queryOrderTool,
                             GetHotProductsTool getHotProductsTool, SearchKnowledgeTool searchKnowledgeTool) {
        this.toolCallAgent = toolCallAgent;

        toolCallAgent.registerTool(SearchProductsTool.toolDef(), searchProductsTool::execute);
        toolCallAgent.registerTool(GetProductDetailTool.toolDef(), getProductDetailTool::execute);
        toolCallAgent.registerTool(QueryOrderTool.toolDef(), queryOrderTool::execute);
        toolCallAgent.registerTool(GetHotProductsTool.toolDef(), getHotProductsTool::execute);
        toolCallAgent.registerTool(SearchKnowledgeTool.toolDef(), searchKnowledgeTool::execute);
    }

    /** 流式对话，支持多轮记忆 */
    public SseEmitter chatStream(String chatId, String userMessage, Integer userId) {
        SseEmitter emitter = new SseEmitter(300_000L);

        // P0: 获取或创建会话历史
        String id = chatId != null ? chatId : "default";
        List<Map<String, String>> history = sessions.get(id, k -> new ArrayList<>());
        if (history == null) history = new ArrayList<>();

        // 首次对话注入系统提示词
        if (history.isEmpty()) {
            history.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            if (userId != null) {
                history.add(Map.of("role", "system",
                        "content", "当前用户ID为 " + userId + "，查询订单时请使用此ID。"));
            }
        }

        // 追加用户消息
        history.add(Map.of("role", "user", "content", userMessage));

        // 裁剪历史保持在 MAX_HISTORY 条以内（保留 system prompt）
        List<Map<String, String>> systemMsgs = history.stream()
                .filter(m -> "system".equals(m.get("role"))).toList();
        List<Map<String, String>> dialogMsgs = history.stream()
                .filter(m -> !"system".equals(m.get("role"))).toList();
        if (dialogMsgs.size() > MAX_HISTORY) {
            dialogMsgs = new ArrayList<>(dialogMsgs.subList(
                    dialogMsgs.size() - MAX_HISTORY, dialogMsgs.size()));
        }
        List<Map<String, String>> messages = new ArrayList<>(systemMsgs);
        messages.addAll(dialogMsgs);

        new Thread(() -> {
            try {
                toolCallAgent.chatStream(messages, new ToolCallAgent.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        try { emitter.send(SseEmitter.event().data(token)); }
                        catch (IOException e) { /* 客户端断开 */ }
                    }
                    @Override
                    public void onComplete() {
                        try { emitter.send(SseEmitter.event().data("[DONE]")); emitter.complete(); }
                        catch (IOException e) { /* ignore */ }
                    }
                    @Override
                    public void onError(String error) {
                        try { emitter.send(SseEmitter.event().data("[ERROR] " + error)); emitter.complete(); }
                        catch (IOException e) { /* ignore */ }
                    }
                });
            } catch (Exception e) {
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            }
        }).start();

        return emitter;
    }

    /** 清除指定会话 */
    public void clearSession(String chatId) {
        if (chatId != null) sessions.invalidate(chatId);
    }
}
