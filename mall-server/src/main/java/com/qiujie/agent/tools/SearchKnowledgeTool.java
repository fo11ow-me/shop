package com.qiujie.agent.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qiujie.rag.KnowledgeBaseService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 知识库检索工具 — 搜索售后政策、配送规则、常见问题等
 *
 * @author qiujie
 */
@Component
public class SearchKnowledgeTool {

    private final KnowledgeBaseService knowledgeBase;

    public SearchKnowledgeTool(KnowledgeBaseService knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public String execute(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        String query = args.getStr("query", "");
        if (query.isBlank()) return "请提供要查询的问题";
        List<String> results = knowledgeBase.search(query);
        if (results.isEmpty()) return "未找到相关知识库内容";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append("【相关").append(i + 1).append("】").append(results.get(i)).append("\n");
        }
        return sb.toString();
    }

    public static Map<String, Object> toolDef() {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", "searchKnowledge",
                "description", "搜索商城知识库，获取退货/换货/退款政策、配送说明、支付方式、发票规则、常见问题等",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "query", Map.of("type", "string", "description", "用户问题的关键词或完整问题")
                    ),
                    "required", List.of("query")
                )
            )
        );
    }
}
