# 秒杀功能 — 实施任务

## P0 — 必须实施

### Task 1: MQ 死信队列 + 重试机制
**文件:** `RabbitMQConfig.java`, `SeckillMessageListener.java`

- [ ] 1.1 声明 `seckill.dlx.exchange`（DirectExchange）和 `seckill.dlx.queue`
- [ ] 1.2 原 `seckill.order.queue` 绑定 DLX，设置 `x-dead-letter-exchange` 和 `x-dead-letter-routing-key`
- [ ] 1.3 `SeckillMessageListener` 添加重试计数（通过消息头 `x-retry-count`）
- [ ] 1.4 消费失败 → 重试 3 次（`basicNack` 重新入队 → 第 4 次 `basicReject` → DLQ）
- [ ] 1.5 DLQ 消费失败时回滚 Redis 库存 + 标记秒杀失败

**验证:** 模拟 MQ 消费异常，确认消息 3 次重试后进入 DLQ，Redis 库存正确回滚

### Task 2: 服务端时间端点
**文件:** `SeckillController.java`, `Seckill.vue`

- [ ] 2.1 新增 `GET /portal/seckill/server-time` 返回 `System.currentTimeMillis()`
- [ ] 2.2 前端 `Seckill.vue` — `onMounted` 时校准 `serverOffset`
- [ ] 2.3 倒计时逻辑改用 `Date.now() + serverOffset`
- [ ] 2.4 安全配置：`/portal/seckill/server-time` 加入 `permitAll()`

**验证:** 手动修改客户端时间 ±30 秒，确认倒计时基于服务端时间

### Task 3: 乐观锁启用
**文件:** `SeckillSession.java`, `SeckillSessionMapper.xml`

- [ ] 3.1 `SeckillSession` 实体添加 `@Version private Integer version;`
- [ ] 3.2 `decrementStock` SQL 添加 `AND version = #{version}` 条件
- [ ] 3.3 更新失败（version 冲突）时，service 层捕获异常并回滚 Redis

**验证:** 并发测试脚本验证 version 冲突时库存一致性

## P1 — 建议实施

### Task 4: 消费者幂等性加固
**文件:** `SeckillMessageListener.java`

- [ ] 4.1 幂等 Key 从 `seckill:processed:{sessionId}:{userId}` 改为 `seckill:processed:{messageId}`
- [ ] 4.2 消息头中携带唯一 `messageId`（`correlationId`）
- [ ] 4.3 处理前 SETNX，已处理则跳过

### Task 5: 监控埋点
**文件:** `SeckillServiceImpl.java`, `SeckillMessageListener.java`

- [ ] 5.1 秒杀执行：记录 QPS（成功/失败/重复）
- [ ] 5.2 库存变化：记录初始库存 → 最终库存
- [ ] 5.3 MQ 消费：记录成功/重试/DLQ 数量
- [ ] 5.4 异常：记录 DLQ 堆积告警

## 依赖关系

```
Task 1 (DLQ) ──┐
               ├── 无依赖，可并行
Task 2 (时间) ─┤
               │
Task 3 (乐观锁)┘

Task 4 (幂等) ←── 依赖 Task 1 (需要 messageId)
Task 5 (监控) ←── 依赖 Task 1-3

Task 4 ── 并行 ── Task 5
```

## 验证清单

- [ ] 秒杀正常流程：扣减 → MQ → 订单创建成功
- [ ] MQ 消费失败：3 次重试 → DLQ → 库存回滚
- [ ] 客户端时间偏差 ±30s：倒计时基于服务端时间
- [ ] 并发库存扣减：超卖 0 单
- [ ] Redis 缓存失效后：降级查询 MySQL 数据
