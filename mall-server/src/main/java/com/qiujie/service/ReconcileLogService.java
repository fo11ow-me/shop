package com.qiujie.service;

import com.qiujie.entity.ReconcileLog;
import com.qiujie.mapper.ReconcileLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对账日志异步写入服务 — 不阻塞秒杀主流程
 *
 * @author qiujie
 */
@Service
public class ReconcileLogService {

    private static final Logger log = LoggerFactory.getLogger(ReconcileLogService.class);
    private final ReconcileLogMapper reconcileLogMapper;

    public ReconcileLogService(ReconcileLogMapper reconcileLogMapper) {
        this.reconcileLogMapper = reconcileLogMapper;
    }

    @Async
    public void log(Integer seckillId, Integer userId, String operation, Integer stockBefore, Integer stockAfter) {
        try {
            ReconcileLog record = new ReconcileLog();
            record.setSeckillId(seckillId);
            record.setUserId(userId);
            record.setOperation(operation);
            record.setStockBefore(stockBefore);
            record.setStockAfter(stockAfter);
            record.setCreateTime(LocalDateTime.now());
            reconcileLogMapper.insert(record);
        } catch (Exception e) {
            log.warn("对账日志写入失败: seckillId={} userId={}", seckillId, userId);
        }
    }
}
