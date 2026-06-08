# 秒杀功能 — 设计文档

## 1. 架构概览

```
用户浏览器                  后端服务                     基础设施
───────────              ──────────                   ──────────
┌──────────┐   GET      ┌──────────────┐   缓存30s    ┌─────────┐
│ Vue 前端  │ ◄──────── │ SeckillCtrl  │ ◄────────── │  Redis   │
│ Seckill  │           │ /portal/     │             │ session  │
│ .vue     │  server-  │ seckill/*    │             │  list    │
└────┬─────┘  time校准  └──────┬───────┘             └─────────┘
     │                        │
     │ POST execute            │
     │ (JWT)                   ▼
     │               ┌────────────────┐
     │               │ SeckillService │
     │  轮询(2s×30)  │                │
     │ ◄──────────── │ Lua 脚本原子扣  │──► Redis stock
     ▼               │ 防重复检查      │──► Redis dedup
┌──────────┐        │ 发 MQ 消息      │──► RabbitMQ
│ 结果弹窗  │        └───────┬────────┘       │
└──────────┘                │                │
                     ┌──────▼────────┐       │
                     │ MQ Listener   │       │
                     │ 重试3次       │◄──────┘
                     │ 幂等处理      │──► Redis processed
                     │ 创建订单      │──► MySQL
                     │ 乐观锁扣库存  │──► MySQL UPDATE
                     │ 失败→DLQ     │──► Dead Letter Queue
                     │ 回滚Redis库存 │──► Redis restore
                     └───────────────┘
```

## 2. 数据流 — 秒杀执行详细时序

```
Client          Controller       Service         Redis           MQ           MySQL
  │                 │               │               │             │              │
  │ POST /execute   │               │               │             │              │
  │────────────────►│               │               │             │              │
  │                 │ execute()     │               │             │              │
  │                 │──────────────►│               │             │              │
  │                 │               │ EVAL Lua      │             │              │
  │                 │               │──────────────►│             │              │
  │                 │               │  OK/FAIL      │             │              │
  │                 │               │◄──────────────│             │              │
  │                 │               │               │             │              │
  │                 │               │ publish msg   │             │              │
  │                 │               │─────────────────────────────►│              │
  │  202 Accepted   │               │               │             │              │
  │◄────────────────│               │               │             │              │
  │                 │               │               │             │              │
  │ GET /result(id) │               │               │   ┌───────►│              │
  │ (2s interval)   │               │   轮询结果    │   │        │              │
  │                 │               │◄──────────────│───┘ consume │              │
  │                 │               │               │             │ UPDATE stock │
  │                 │               │               │             │─────────────►│
  │                 │               │               │             │ INSERT order │
  │                 │               │               │             │─────────────►│
  │  result: OK     │               │               │             │              │
  │◄────────────────│               │               │             │              │
```

## 3. 组件设计

### 3.1 MQ 死信队列

```java
// RabbitMQConfig — 秒杀队列绑定 DLX
@Bean
public Queue seckillOrderQueue() {
    return QueueBuilder.durable("seckill.order.queue")
        .deadLetterExchange("seckill.dlx.exchange")
        .deadLetterRoutingKey("seckill.dlx")
        .build();
}

@Bean
public Queue seckillDeadLetterQueue() {
    return new Queue("seckill.dlx.queue", true);
}

@Bean
public DirectExchange seckillDlxExchange() {
    return new DirectExchange("seckill.dlx.exchange");
}

@Bean
public Binding seckillDlxBinding() {
    return BindingBuilder.bind(seckillDeadLetterQueue())
        .to(seckillDlxExchange()).with("seckill.dlx");
}
```

### 3.2 消费者重试机制

```java
// SeckillMessageListener
@RabbitListener(queues = "seckill.order.queue")
public void handle(Message message, Channel channel) {
    long deliveryTag = message.getMessageProperties().getDeliveryTag();
    try {
        processSeckillOrder(message);
        channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
        int retryCount = getRetryCount(message);
        if (retryCount < 3) {
            // 递增重试，NACK 重新入队
            channel.basicNack(deliveryTag, false, true);
        } else {
            // 3次后拒绝，进入 DLQ
            channel.basicReject(deliveryTag, false);
            // 回滚 Redis 库存
            rollbackStock(message);
        }
    }
}
```

### 3.3 服务端时间端点

```java
// SeckillController
@GetMapping("/server-time")
public ResponseDTO<Long> serverTime() {
    return Response.success(System.currentTimeMillis());
}
```

前端校准逻辑:
```javascript
// Seckill.vue — onMounted
let serverOffset = 0
onMounted(async () => {
    const start = Date.now()
    const res = await request.get('/seckill/server-time')
    const roundTrip = Date.now() - start
    serverOffset = res.data - (start + roundTrip / 2)
    loadSessions()
})

// 校准后的服务器时间
function serverNow() { return Date.now() + serverOffset }
```

### 3.4 乐观锁

```java
// SeckillSession 实体
@Version
private Integer version;
```

```xml
<!-- SeckillSessionMapper.xml -->
<update id="decrementStock">
    UPDATE sms_seckill_session
    SET seckill_stock = seckill_stock - #{delta},
        version = version + 1
    WHERE id = #{sessionId}
      AND seckill_stock >= #{delta}
      AND version = #{version}
</update>
```

## 4. 接口设计

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/portal/seckill/sessions` | 否 | 进行中场次（缓存30s） |
| GET | `/portal/seckill/sessions/upcoming` | 否 | 即将开始场次（缓存30s） |
| POST | `/portal/seckill/execute` | JWT | 执行秒杀 |
| GET | `/portal/seckill/result/{sessionId}` | JWT | 轮询秒杀结果 |
| **GET** | **`/portal/seckill/server-time`** | 否 | **新增: 服务端时间戳** |

## 5. 错误处理矩阵

| 场景 | 处理 |
|------|------|
| 库存不足 | Lua 脚本返回 -1，前端提示"已售罄" |
| 重复下单 | Lua 脚本返回 -2，前端提示"请勿重复下单" |
| MQ 投递失败 | 回滚 Redis 库存 + 删除防重标记 |
| MQ 消费失败 (第1-2次) | `basicNack` 重新入队重试 |
| MQ 消费失败 (第3次) | `basicReject` → DLQ + 回滚 Redis 库存 |
| MySQL 扣减失败 (version冲突) | 回滚 Redis 库存 + 标记秒杀失败 |
| 轮询超时 (60秒) | 提示"排队超时，请查看订单" |

## 6. Redis Key 设计

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `seckill:stock:{sessionId}` | string | 场次结束 | 秒杀库存缓存 |
| `seckill:order:{sessionId}:{userId}` | string | 86400s | 防重复下单 |
| `seckill:processed:{sessionId}:{userId}` | string | 120s | MQ 幂等 |
| `seckill:result:{sessionId}:{userId}` | string | 120s | 轮询结果 |
| `cache:seckill:active` | string | 30s | 进行中场次缓存 |
| `cache:seckill:upcoming` | string | 30s | 即将开始场次缓存 |
