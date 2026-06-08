package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.SeckillService;
import com.qiujie.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

    public SeckillController(SeckillService seckillService, JwtUtil jwtUtil) {
        this.seckillService = seckillService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前进行中的秒杀场次
     *
     * @return 秒杀场次列表（含商品信息和实时库存）
     */
    @Operation(summary = "获取进行中的秒杀场次")
    @GetMapping("/sessions")
    public ResponseDTO<List<Map<String, Object>>> activeSessions() {
        return Response.success(seckillService.getActiveSessions());
    }

    /**
     * 获取即将开始的秒杀场次
     *
     * @return 秒杀场次列表（含商品信息）
     */
    @Operation(summary = "获取即将开始的秒杀场次")
    @GetMapping("/sessions/upcoming")
    public ResponseDTO<List<Map<String, Object>>> upcomingSessions() {
        return Response.success(seckillService.getUpcomingSessions());
    }

    /**
     * 执行秒杀扣库存并异步创建订单
     *
     * @param sessionId   秒杀场次 ID
     * @param authorization 用户令牌
     * @return 排队成功提示
     */
    @Operation(summary = "执行秒杀")
    @PostMapping("/execute")
    public ResponseDTO<Void> execute(@RequestParam Integer sessionId,
                                     @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Integer userId = jwtUtil.extractUserId(token, "/portal");
        seckillService.execute(sessionId, userId);
        return Response.ok("已加入排队");
    }

    /**
     * 查询秒杀执行结果（供前端轮询）
     *
     * @param sessionId   秒杀场次 ID
     * @param authorization 用户令牌
     * @return { status: 0=排队中, 1=成功, -1=失败, msg: 描述 }
     */
    @Operation(summary = "查询秒杀结果")
    @GetMapping("/result/{sessionId}")
    public ResponseDTO<Map<String, Object>> result(@PathVariable Integer sessionId,
                                                    @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Integer userId = jwtUtil.extractUserId(token, "/portal");
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
