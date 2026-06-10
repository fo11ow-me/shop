package com.qiujie.controller.admin;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.SeckillSession;
import com.qiujie.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 秒杀管理控制器（后台）
 *
 * @author qiujie
 */
@Tag(name = "秒杀管理")
@Profile("!test")
@RestController
@RequestMapping("/admin/seckill")
public class AdminSeckillController {

    private final SeckillService seckillService;

    public AdminSeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @Operation(summary = "秒杀场次分页列表")
    @GetMapping("/list")
    public ResponseDTO<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Integer status) {
        return Response.success(seckillService.listPage(current, size, status));
    }

    @Operation(summary = "创建秒杀场次")
    @PostMapping
    public ResponseDTO<Void> create(@Valid @RequestBody SeckillSession session) {
        seckillService.create(session);
        return Response.ok("创建成功");
    }

    @Operation(summary = "更新秒杀场次")
    @PutMapping
    public ResponseDTO<Void> update(@Valid @RequestBody SeckillSession session) {
        seckillService.updateSession(session);
        return Response.ok("更新成功");
    }

    @Operation(summary = "删除秒杀场次")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        seckillService.deleteSession(id);
        return Response.ok("删除成功");
    }
}
