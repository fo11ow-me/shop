package com.qiujie.agent.tools;

import cn.hutool.json.JSONUtil;
import com.qiujie.util.SalesRankService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 热销排行工具
 *
 * @author qiujie
 */
@Component
public class GetHotProductsTool {

    private final SalesRankService salesRankService;

    public GetHotProductsTool(SalesRankService salesRankService) {
        this.salesRankService = salesRankService;
    }

    public String execute(String argsJson) {
        List<Map<String, Object>> top5 = salesRankService.getTop5();
        if (top5 == null || top5.isEmpty()) return "暂无热销数据";
        return JSONUtil.toJsonStr(top5);
    }

    public static Map<String, Object> toolDef() {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", "getHotProducts",
                "description", "获取当前销量排行榜前5名商品，返回商品ID和销量数据",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of()
                )
            )
        );
    }
}
