package com.qiujie.agent.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qiujie.service.ProductService;
import com.qiujie.vo.ProductVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 商品详情工具
 *
 * @author qiujie
 */
@Component
public class GetProductDetailTool {

    private final ProductService productService;

    public GetProductDetailTool(ProductService productService) {
        this.productService = productService;
    }

    public String execute(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        int productId = args.getInt("productId", 0);
        if (productId <= 0) return "请提供有效的商品ID";
        try {
            ProductVO product = productService.detail(productId);
            if (product == null) return "未找到商品 ID=" + productId;
            return JSONUtil.toJsonStr(product);
        } catch (Exception e) {
            return "查询失败: " + e.getMessage();
        }
    }

    public static Map<String, Object> toolDef() {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", "getProductDetail",
                "description", "根据商品ID获取商品详细信息，包括名称、价格、库存、图片、描述",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "productId", Map.of("type", "integer", "description", "商品ID")
                    ),
                    "required", List.of("productId")
                )
            )
        );
    }
}
