package com.qiujie.agent.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qiujie.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 搜索商品工具
 *
 * @author qiujie
 */
@Component
public class SearchProductsTool {

    private final ProductService productService;

    public SearchProductsTool(ProductService productService) {
        this.productService = productService;
    }

    public String execute(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        String keyword = args.getStr("keyword", "");
        Map<String, Object> result = productService.search(keyword, 1, 5);
        if (result == null) return "未找到相关商品";
        List<?> records = (List<?>) result.get("records");
        if (records == null || records.isEmpty()) return "未找到与\"" + keyword + "\"相关的商品";
        return JSONUtil.toJsonStr(records);
    }

    public static Map<String, Object> toolDef() {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", "searchProducts",
                "description", "根据关键词搜索商品，返回商品名称、价格、库存等信息",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "keyword", Map.of("type", "string", "description", "搜索关键词")
                    ),
                    "required", List.of("keyword")
                )
            )
        );
    }
}
