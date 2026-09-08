package com.qiujie.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.qiujie.annotation.RateLimit;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动控制器（门户端）
 *
 * @author qiujie
 */
@Tag(name = "秒杀活动")
@Profile("!test")
@RestController
@RequestMapping("/portal/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @Operation(summary = "获取进行中的秒杀场次")
    @GetMapping("/sessions")
    public ResponseDTO<List<Map<String, Object>>> activeSessions() {
        return Response.success(seckillService.getActiveSessions());
    }

    @Operation(summary = "获取即将开始的秒杀场次")
    @GetMapping("/sessions/upcoming")
    public ResponseDTO<List<Map<String, Object>>> upcomingSessions() {
        return Response.success(seckillService.getUpcomingSessions());
    }

    @RateLimit(key = "rate:seckill:execute:", limit = 3, window = 60, message = "秒杀请求过于频繁，请稍后再试")
    @Operation(summary = "执行秒杀")
    @PostMapping("/execute")
    public ResponseDTO<Void> execute(@RequestParam Integer sessionId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        seckillService.execute(sessionId, userId);
        return Response.ok("已加入排队");
    }

    @Operation(summary = "查询秒杀结果")
    @GetMapping("/result/{sessionId}")
    public ResponseDTO<Map<String, Object>> result(@PathVariable Integer sessionId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        return Response.success(seckillService.getResult(sessionId, userId));
    }

    /**
     * 服务端时间戳（供前端校准倒计时）
     *
     * @return 当前时间的毫秒时间戳
     */
    @Operation(summary = "服务端时间")
    @GetMapping("/server-time")
    public ResponseDTO<Long> serverTime() {
        return Response.success(System.currentTimeMillis());
    }
}
