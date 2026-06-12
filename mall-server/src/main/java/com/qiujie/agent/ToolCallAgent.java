package com.qiujie.agent;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 带工具调用的 Agent — 调用 DeepSeek API（OpenAI 兼容格式）
 *
 * @author qiujie
 */
@Component
public class ToolCallAgent {

    private static final Logger log = LoggerFactory.getLogger(ToolCallAgent.class);

    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    private final List<Map<String, Object>> toolDefs = new ArrayList<>();
    private final java.util.Map<String, Function<String, String>> toolExecutors = new java.util.LinkedHashMap<>();

    // TerminateTool: Agent 完成任务后自主终止，不依赖 maxSteps 硬截断
    private static final Map<String, Object> TERMINATE_TOOL_DEF = Map.of(
        "type", "function",
        "function", Map.of(
            "name", "doTerminate",
            "description", "任务已完成，结束当前对话。当你已经获得足够信息并回答了用户问题后调用。",
            "parameters", Map.of("type", "object", "properties", Map.of())
        )
    );

    public ToolCallAgent(@Value("${deepseek.api-key}") String apiKey,
                         @Value("${deepseek.base-url}") String baseUrl,
                         @Value("${deepseek.model}") String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        toolDefs.add(TERMINATE_TOOL_DEF);
        toolExecutors.put("doTerminate", args -> "OK");
    }

    /** 注册工具：定义 + 执行函数 */
    public void registerTool(Map<String, Object> toolDef, Function<String, String> executor) {
        toolDefs.add(toolDef);
        toolExecutors.put((String) ((Map<String, Object>) toolDef.get("function")).get("name"), executor);
    }

    /** ReAct 循环：think → act → observe → 重复，直到 LLM 决定停止 */
    public void chatStream(List<Map<String, String>> messages, StreamCallback callback) {
        log.debug("ReAct start — messages: {}", messages.size());
        for (int step = 0; step < 10; step++) {
            JSONObject resp = callDeepSeek(messages, toolDefs);
            if (resp == null) {
                callback.onError("API 调用失败");
                return;
            }

            JSONObject choice = resp.getJSONArray("choices").getJSONObject(0);
            String finishReason = choice.getStr("finish_reason");
            JSONObject message = choice.getJSONObject("message");
            log.debug("ReAct step {} — finish_reason: {}", step, finishReason);

            // 如果 LLM 调用了工具
            if ("tool_calls".equals(finishReason) || message.containsKey("tool_calls")) {
                JSONArray toolCalls = message.getJSONArray("tool_calls");
                if (toolCalls == null || toolCalls.isEmpty()) {
                    callback.onError("工具调用为空");
                    return;
                }
                messages.add(Map.of("role", "assistant", "content", message.toString()));
                boolean terminated = false;
                for (int i = 0; i < toolCalls.size(); i++) {
                    JSONObject tc = toolCalls.getJSONObject(i);
                    String fnName = tc.getJSONObject("function").getStr("name");
                    String fnArgs = tc.getJSONObject("function").getStr("arguments");
                    log.debug("ReAct tool call: {} args: {}", fnName, fnArgs);
                    if ("doTerminate".equals(fnName)) {
                        terminated = true;
                        break;
                    }
                    String result = executeTool(fnName, fnArgs);
                    messages.add(Map.of("role", "tool", "content", result,
                            "tool_call_id", tc.getStr("id")));
                }
                if (terminated) {
                    log.debug("ReAct terminated by agent at step {}", step);
                    break;
                }
                // 继续循环：LLM 可以再次调用工具
            } else {
                // LLM 决定直接回复 → 输出并结束
                log.debug("ReAct complete at step {}", step);
                streamContent(resp, callback);
                return;
            }
        }
        callback.onError("思考步骤过多，请尝试简化问题");
    }

    private String executeTool(String name, String args) {
        Function<String, String> fn = toolExecutors.get(name);
        if (fn == null) return "{\"error\": \"Unknown tool: " + name + "\"}";
        try {
            return fn.apply(args);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private JSONObject callDeepSeek(List<Map<String, String>> messages, List<Map<String, Object>> tools) {
        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("messages", messages);
        if (tools != null && !tools.isEmpty()) {
            body.set("tools", tools);
        }
        body.set("stream", false);

        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            String respBody = response.body() != null ? response.body().string() : "";
            return JSONUtil.parseObj(respBody);
        } catch (IOException e) {
            return null;
        }
    }

    /** 从非流式响应中提取 content 并逐字回调 */
    private void streamContent(JSONObject resp, StreamCallback callback) {
        JSONArray choices = resp.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            callback.onComplete();
            return;
        }
        String content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getStr("content");
        if (content == null) {
            callback.onComplete();
            return;
        }
        // 模拟流式输出：按字符逐字发送
        for (int i = 0; i < content.length(); i++) {
            callback.onToken(String.valueOf(content.charAt(i)));
            try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        callback.onComplete();
    }

    /** 流式回调接口 */
    public interface StreamCallback {
        void onToken(String token);
        void onComplete();
        void onError(String error);
    }
}
