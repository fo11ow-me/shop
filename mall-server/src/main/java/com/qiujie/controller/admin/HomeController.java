package com.qiujie.controller.admin;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.HomeService;
import com.qiujie.vo.CategorySalesVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @Operation(summary = "首页统计")
    @GetMapping("/count")
    public ResponseDTO<Map<String, Object>> count() {
        return Response.success(this.homeService.getCount());
    }

    @Operation(summary = "趋势数据（最近N天的每日订单数和销售额）")
    @GetMapping("/trend")
    public ResponseDTO<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") Integer days) {
        return Response.success(this.homeService.getTrend(days));
    }

    @Operation(summary = "分类销量排行")
    @GetMapping("/category-sales")
    public ResponseDTO<List<CategorySalesVO>> categorySales() {
        return Response.success(this.homeService.getCategorySales());
    }
}
