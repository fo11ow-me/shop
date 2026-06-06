# Redis 功能增强 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 mall 项目补充 5 个 Redis 相关能力：验证码键隔离、JWT 黑名单、秒杀后端、商品分类缓存、接口限流。

**Architecture:** 5 个子功能按依赖关系排序，验证码 → JWT → 缓存 → 限流 → 秒杀。每个子功能独立可测，后端新增功能均需测试覆盖。

**Tech Stack:** Spring Boot 3.2.5, MyBatis-Plus 3.5.5, Redis (Jedis/Lettuce), JWT (jjwt 0.12.5), Vue 3 + Axios

---

## 文件结构

### 新建文件
- `mall-server/src/main/java/com/qiujie/entity/SeckillSession.java` — 秒杀场次实体
- `mall-server/src/main/java/com/qiujie/mapper/SeckillSessionMapper.java` — 秒杀 Mapper
- `mall-server/src/main/resources/mapper/SeckillSessionMapper.xml` — SQL 映射
- `mall-server/src/main/java/com/qiujie/service/SeckillService.java` — 秒杀服务接口
- `mall-server/src/main/java/com/qiujie/service/impl/SeckillServiceImpl.java` — 秒杀服务实现
- `mall-server/src/main/java/com/qiujie/controller/portal/SeckillController.java` — 秒杀控制器
- `mall-server/src/main/java/com/qiujie/enums/SeckillResultEnum.java` — 秒杀结果枚举
- `mall-server/src/main/java/com/qiujie/annotation/RateLimit.java` — 限流注解
- `mall-server/src/main/java/com/qiujie/interceptor/RateLimitInterceptor.java` — 限流拦截器
- `mall-server/src/main/java/com/qiujie/config/WebMvcConfig.java` — 注册拦截器

### 修改文件（后端）
- `mall-server/src/main/java/com/qiujie/service/AuthService.java`
- `mall-server/src/main/java/com/qiujie/service/impl/AuthServiceImpl.java`
- `mall-server/src/main/java/com/qiujie/controller/portal/AuthController.java`
- `mall-server/src/main/java/com/qiujie/controller/admin/AuthController.java`
- `mall-server/src/main/java/com/qiujie/util/JwtUtil.java`
- `mall-server/src/main/java/com/qiujie/filter/JwtAuthenticationFilter.java`
- `mall-server/src/main/java/com/qiujie/config/SecurityConfig.java`
- `mall-server/src/main/java/com/qiujie/service/impl/ProductServiceImpl.java`
- `mall-server/src/main/java/com/qiujie/service/impl/CategoryServiceImpl.java`
- `mall-server/src/main/java/com/qiujie/controller/admin/ProductController.java`
- `mall-server/src/main/java/com/qiujie/controller/admin/CategoryController.java`
- `mall-server/src/main/java/com/qiujie/constants/RedisConstants.java`
- `mall-server/src/main/java/com/qiujie/enums/BusinessStatusEnum.java`
- `mall-server/src/main/resources/application.yml` — 新增 jwt.expiration 配置确认
- `docs/mall.sql` — 新增秒杀表

### 修改文件（前端）
- `mall-portal/src/views/Login.vue` — 验证码 uuid 传递
- `mall-portal/src/api/auth.js` — 验证码接口返回 header 中的 uuid
- `mall-admin/src/views/Login.vue` — 验证码 uuid 传递
- `mall-admin/src/api/auth.js` — 同上

### 测试文件
- `mall-server/src/test/java/com/qiujie/api/AuthApiTest.java` — 更新适配
- 其余测试在各自 task 中通过 Maven 运行验证

---

### Task 1: 验证码键隔离 — 后端

**Files:**
- Modify: `mall-server/src/main/java/com/qiujie/service/AuthService.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/impl/AuthServiceImpl.java`
- Modify: `mall-server/src/main/java/com/qiujie/controller/portal/AuthController.java`
- Modify: `mall-server/src/main/java/com/qiujie/controller/admin/AuthController.java`
- Modify: `mall-server/src/test/java/com/qiujie/api/AuthApiTest.java`

- [ ] **Step 1: 修改 AuthService 接口**

`AuthService.java` 中 `getVerificationCode` 返回类型改为 `String`（返回 uuid），`login` 方法签名增加 `uuid` 参数：

```java
// AuthService.java
package com.qiujie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface AuthService extends IService<User> {

    void register(String code, String password, String phone);

    Map<String, Object> login(String code, String password, String verificationCode, String uuid, String pathPrefix);

    String getVerificationCode(HttpServletResponse response) throws IOException;
}
```

- [ ] **Step 2: 实现验证码键隔离逻辑**

修改 `AuthServiceImpl.java`：

```java
// AuthServiceImpl.java — getVerificationCode 方法替换为：
@Override
public String getVerificationCode(HttpServletResponse response) throws IOException {
    ValidateCode validateCode = ValidateCodeUtil.generateValidateCode();
    String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
    redisUtil.set("validate:code:" + uuid, validateCode.getCode(), 60);
    ImageIO.write(validateCode.getImage(), "jpeg", response.getOutputStream());
    return uuid;
}

// login 方法修改为接收 uuid 参数：
@Override
public Map<String, Object> login(String code, String password, String verificationCode, String uuid, String pathPrefix) {
    String codeInRedis = (String) redisUtil.get("validate:code:" + uuid);
    // ... 后续逻辑不变
```

验证码 key 从 `"validate:code"` 改为 `"validate:code:" + uuid`。

Admin 端统一使用同一个 `AuthServiceImpl`，校验逻辑一致。Admin 登录通过 `/admin` 前缀区分 JWT secret。

- [ ] **Step 3: 修改 portal AuthController**

```java
// portal/AuthController.java — getVerificationCode 改为返回 uuid
@Operation(summary = "获取验证码")
@GetMapping("/verificationCode")
public ResponseDTO<Map<String, String>> getVerificationCode(HttpServletResponse response) throws IOException {
    String uuid = authService.getVerificationCode(response);
    return Response.success(Map.of("uuid", uuid));
}

// login 方法改为接收 uuid 参数
@Operation(summary = "登录")
@PostMapping("/login")
public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, String> params) {
    return Response.success(authService.login(
        params.get("code"), params.get("password"),
        params.get("verificationCode"), params.get("uuid"), "/portal"));
}
```

- [ ] **Step 4: 修改 admin AuthController**

同 portal 端一致：

```java
// admin/AuthController.java
@Operation(summary = "获取验证码")
@GetMapping("/verificationCode")
public ResponseDTO<Map<String, String>> getVerificationCode(HttpServletResponse response) throws IOException {
    String uuid = authService.getVerificationCode(response);
    return Response.success(Map.of("uuid", uuid));
}

@Operation(summary = "登录")
@PostMapping("/login")
public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, String> params) {
    return Response.success(authService.login(
        params.get("code"), params.get("password"),
        params.get("verificationCode"), params.get("uuid"), "/admin"));
}
```

- [ ] **Step 5: 更新 AuthApiTest 测试**

测试中验证码准备改为新键格式：

```java
// AuthApiTest.java — setUp 或测试方法中
// 旧: redisTemplate.opsForValue().set("validate:code", "1234");
// 新: redisTemplate.opsForValue().set("validate:code:test-uuid-001", "1234");

// login 请求增加 uuid 字段：
loginRequest.put("uuid", "test-uuid-001");
```

- [ ] **Step 6: 运行后端测试验证**

```bash
cd mall-server && mvn test -Dtest="AuthApiTest"
```

预期：所有 AuthApiTest 测试通过。

---

### Task 2: 验证码键隔离 — 前端

**Files:**
- Modify: `mall-portal/src/api/auth.js`
- Modify: `mall-portal/src/views/Login.vue`
- Modify: `mall-admin/src/api/auth.js`
- Modify: `mall-admin/src/views/Login.vue`

- [ ] **Step 1: 修改 portal auth.js API**

```javascript
// mall-portal/src/api/auth.js
import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getVerificationCode = () => request.get('/auth/verificationCode', { responseType: 'blob' })
export const getVerificationCodeUuid = () => request.get('/auth/verificationCode')
```

新增 `getVerificationCodeUuid` 方法（不带 `responseType: 'blob'`），返回 JSON 获取 uuid。

- [ ] **Step 2: 修改 portal Login.vue**

```javascript
// mall-portal/src/views/Login.vue — script setup 部分
import { login, getVerificationCode, getVerificationCodeUuid } from '../api'

let verificationUuid = ''

const refreshCode = async () => {
  try {
    // 先拿到 uuid
    const uuidRes = await getVerificationCodeUuid()
    verificationUuid = uuidRes.data.uuid
    // 再拿图片
    const res = await getVerificationCode()
    const url = URL.createObjectURL(res.data)
    verificationCodeImg.value = url
    const img = new Image()
    img.onload = () => URL.revokeObjectURL(url)
    img.src = url
  } catch {}
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ ...form, uuid: verificationUuid })
    authStore.loginSuccess(res.data.token, res.data.user)
    router.push('/')
  } catch {} finally { loading.value = false }
}
```

- [ ] **Step 3: 修改 admin auth.js API**

```javascript
// mall-admin/src/api/auth.js
import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)

export const getVerificationCode = () => request.get('/auth/verificationCode', {
  responseType: 'blob'
})

export const getVerificationCodeUuid = () => request.get('/auth/verificationCode')
```

- [ ] **Step 4: 修改 admin Login.vue**

```javascript
// mall-admin/src/views/Login.vue — script setup 部分
import { login as apiLogin, getVerificationCode, getVerificationCodeUuid } from '@/api/auth'

let verificationUuid = ''

function refreshCode() {
  getVerificationCodeUuid().then(res => {
    verificationUuid = res.data.uuid
  })
  getVerificationCode().then(res => {
    const url = window.URL.createObjectURL(new Blob([res.data]))
    codeUrl.value = url
    const img = new Image()
    img.onload = () => URL.revokeObjectURL(url)
    img.src = url
  })
}

async function login() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await apiLogin({ ...form, uuid: verificationUuid })
      // ... 后续逻辑不变
```

- [ ] **Step 5: 启动前后端验证登录流程**

```bash
# 启动后端
cd mall-server && mvn spring-boot:run
# 启动管理端
cd mall-admin && npm run dev
# 启动门户
cd mall-portal && npm run dev
```

分别登录管理端和门户，确认验证码功能正常。

---

### Task 3: JWT 黑名单 — JwtUtil 增加 jti 支持

**Files:**
- Modify: `mall-server/src/main/java/com/qiujie/util/JwtUtil.java`

- [ ] **Step 1: 修改 JwtUtil 增加 jti 和黑名单相关方法**

```java
// JwtUtil.java — 在 generateToken 方法中增加 jti
public String generateToken(UserDetails userDetails, String pathPrefix) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", ((CustomUserDetails) userDetails).getUserId());
    claims.put("jti", java.util.UUID.randomUUID().toString().replace("-", ""));
    return generateToken(claims, userDetails, pathPrefix);
}

// 新增方法：提取 jti
public String extractJti(String token, String requestUri) {
    return extractClaim(token, claims -> claims.get("jti", String.class), requestUri);
}

// 新增方法：计算 token 剩余有效秒数
public long extractRemainingSeconds(String token, String requestUri) {
    Date expiration = extractExpiration(token, requestUri);
    long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
    return Math.max(remaining, 0);
}
```

- [ ] **Step 2: 运行已有 JWT 测试确认兼容**

```bash
cd mall-server && mvn test -Dtest="AuthApiTest"
```

预期：测试通过（jti 为新增字段，不影响现有解析逻辑）。

---

### Task 4: JWT 黑名单 — 过滤器与退出接口

**Files:**
- Modify: `mall-server/src/main/java/com/qiujie/filter/JwtAuthenticationFilter.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/AuthService.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/impl/AuthServiceImpl.java`
- Modify: `mall-server/src/main/java/com/qiujie/controller/portal/AuthController.java`
- Modify: `mall-server/src/main/java/com/qiujie/controller/admin/AuthController.java`
- Modify: `mall-server/src/main/java/com/qiujie/config/SecurityConfig.java`
- Modify: `mall-server/src/main/java/com/qiujie/enums/BusinessStatusEnum.java`

- [ ] **Step 1: JwtAuthenticationFilter 增加黑名单检查**

```java
// JwtAuthenticationFilter.java — 注入 RedisUtil
private final RedisUtil redisUtil;

public JwtAuthenticationFilter(CustomUserDetailsService customUserDetailsService,
                               JwtUtil jwtUtil, RedisUtil redisUtil) {
    this.customUserDetailsService = customUserDetailsService;
    this.jwtUtil = jwtUtil;
    this.redisUtil = redisUtil;
}

// doFilterInternal 中，在提取 username 之后、加载用户之前增加黑名单检查：
if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
    // JWT 黑名单检查
    String jti = jwtUtil.extractJti(token, requestUri);
    if (jti != null && redisUtil.hasKey("token:blacklist:" + jti)) {
        filterChain.doFilter(request, response);
        return;
    }
    try {
        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
        // ... 后续逻辑不变
```

- [ ] **Step 2: AuthService 增加 logout 方法**

```java
// AuthService.java 增加方法签名
void logout(String token, String requestUri);
```

- [ ] **Step 3: AuthServiceImpl 实现 logout 方法**

```java
// AuthServiceImpl.java 增加实现
@Override
public void logout(String token, String requestUri) {
    String jti = jwtUtil.extractJti(token, requestUri);
    if (jti != null) {
        long remaining = jwtUtil.extractRemainingSeconds(token, requestUri);
        if (remaining > 0) {
            redisUtil.set("token:blacklist:" + jti, "1", remaining);
        }
    }
}
```

需要注入 `JwtUtil`（如果尚未注入）：

```java
private final JwtUtil jwtUtil;

public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                       RedisUtil redisUtil, AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
    // ... 已有字段 + this.jwtUtil = jwtUtil;
}
```

- [ ] **Step 4: portal 和 admin AuthController 增加 logout 端点**

```java
// portal/AuthController.java 新增
@Operation(summary = "退出登录")
@PostMapping("/logout")
public ResponseDTO<Void> logout(@RequestHeader("Authorization") String authorization) {
    String token = authorization.replace("Bearer ", "");
    authService.logout(token, "/portal");
    return Response.ok("已退出");
}

// admin/AuthController.java 新增（同样逻辑，pathPrefix 用 "/admin"）
@Operation(summary = "退出登录")
@PostMapping("/logout")
public ResponseDTO<Void> logout(@RequestHeader("Authorization") String authorization) {
    String token = authorization.replace("Bearer ", "");
    authService.logout(token, "/admin");
    return Response.ok("已退出");
}
```

- [ ] **Step 5: SecurityConfig 放行 logout 路径**

```java
// SecurityConfig.java — authorizeHttpRequests 中增加两行
.requestMatchers("/portal/auth/logout").permitAll()
.requestMatchers("/admin/auth/logout").permitAll()
```

放在 `/portal/**` 和 `/admin/**` 的 authenticated 规则之前。

- [ ] **Step 6: BusinessStatusEnum 增加黑名单相关错误码**

```java
// BusinessStatusEnum.java 新增
TOKEN_BLACKLISTED(1409, "令牌已失效，请重新登录"),
```

- [ ] **Step 7: 运行测试**

```bash
cd mall-server && mvn test -Dtest="AuthApiTest"
```

---

### Task 5: JWT 黑名单 — 前端退出调用

**Files:**
- Modify: `mall-portal/src/api/auth.js`
- Modify: `mall-portal/src/views/Layout.vue`（退出逻辑所在位置）
- Modify: `mall-admin/src/api/auth.js`
- Modify: `mall-admin/src/stores/modules/token.js` 或退出逻辑所在位置

- [ ] **Step 1: portal 增加 logout API**

```javascript
// mall-portal/src/api/auth.js 增加
export const logout = () => request.post('/auth/logout')
```

- [ ] **Step 2: portal Layout.vue 退出时调用 logout API**

找到 portal Layout.vue 中退出登录的处理函数，在清除本地状态前调用 `logout()`：

```javascript
// 退出处理函数中
import { logout } from '@/api/auth'

const handleLogout = async () => {
  try { await logout() } catch {}
  authStore.logout()
  router.push('/login')
}
```

- [ ] **Step 3: admin 增加 logout API**

```javascript
// mall-admin/src/api/auth.js 增加
export const logout = () => request.post('/auth/logout')
```

- [ ] **Step 4: admin 退出时调用 logout API**

在 admin Header.vue 或 token store 的退出逻辑中调用 `logout()` API。

- [ ] **Step 5: 前后端联调验证**

1. 登录 → 获取 token
2. 调用 `/portal/cart/list` 确认 token 有效（返回 200）
3. 退出登录
4. 再次调用 `/portal/cart/list` 确认 token 已失效（返回 401）

---

### Task 6: 商品/分类缓存 — RedisConstants 与后端改造

**Files:**
- Modify: `mall-server/src/main/java/com/qiujie/constants/RedisConstants.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/impl/ProductServiceImpl.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/impl/CategoryServiceImpl.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/ProductService.java`
- Modify: `mall-server/src/main/java/com/qiujie/service/CategoryService.java`
- Modify: `mall-server/src/main/java/com/qiujie/controller/admin/ProductController.java`
- Modify: `mall-server/src/main/java/com/qiujie/controller/admin/CategoryController.java`

- [ ] **Step 1: RedisConstants 新增缓存常量**

```java
// RedisConstants.java 新增
/** 商品详情缓存前缀 */
public static final String CACHE_PRODUCT_KEY = "cache:product:";
/** 分类树缓存键 */
public static final String CACHE_CATEGORY_TREE_KEY = "cache:category:tree";
/** 首页数据缓存键 */
public static final String CACHE_HOME_KEY = "cache:product:home";
/** 商品缓存 TTL（秒） */
public static final Long CACHE_PRODUCT_TTL = 1800L;
/** 分类缓存 TTL（秒） */
public static final Long CACHE_CATEGORY_TTL = 1800L;
/** 首页缓存 TTL（秒） */
public static final Long CACHE_HOME_TTL = 600L;
```

- [ ] **Step 2: ProductServiceImpl 注入 CacheClient 并改造方法**

```java
// ProductServiceImpl.java — 新增注入
private final CacheClient cacheClient;

public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper,
                          ProductImgMapper productImgMapper, CacheClient cacheClient) {
    // ... 已有赋值
    this.cacheClient = cacheClient;
}

// detail 方法改为缓存优先
@Override
public ProductVO detail(Integer id) {
    return cacheClient.handleCachePenetration(
        CACHE_PRODUCT_KEY, id, ProductVO.class,
        productId -> {
            ProductVO product = productMapper.selectDetailById(productId);
            if (product == null) {
                throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
            }
            return product;
        },
        CACHE_PRODUCT_TTL, TimeUnit.SECONDS
    );
}

// categories 方法改为缓存优先
@Override
public List<Category> categories() {
    String jsonStr = stringRedisTemplate.opsForValue().get(CACHE_CATEGORY_TREE_KEY);
    if (StrUtil.isNotBlank(jsonStr)) {
        return JSONUtil.toList(jsonStr, Category.class);
    }
    List<Category> result = buildCategoryTree();
    if (!result.isEmpty()) {
        cacheClient.set(CACHE_CATEGORY_TREE_KEY, result, CACHE_CATEGORY_TTL, TimeUnit.SECONDS);
    }
    return result;
}

// home 方法改为缓存优先
@Override
public List<Map<String, Object>> home() {
    String jsonStr = stringRedisTemplate.opsForValue().get(CACHE_HOME_KEY);
    if (StrUtil.isNotBlank(jsonStr)) {
        return JSONUtil.toList(jsonStr, Map.class);
    }
    List<Map<String, Object>> result = buildHomeData();
    if (!result.isEmpty()) {
        cacheClient.set(CACHE_HOME_KEY, result, CACHE_HOME_TTL, TimeUnit.SECONDS);
    }
    return result;
}
```

注意：`categories()` 和 `home()` 使用 `StringRedisTemplate`（从 CacheClient 中暴露或独立注入），因为 CacheClient 已注入 StringRedisTemplate。当前 CacheClient 的 set 方法使用 JSONUtil 序列化，可以直接使用。

`ProductServiceImpl` 同时需要注入 `StringRedisTemplate` 用于 categories 和 home 的手动反序列化：

```java
private final StringRedisTemplate stringRedisTemplate;
```

`CacheClient` 中已有 `StringRedisTemplate`，但未暴露。`ProductServiceImpl` 可注入自己的 `StringRedisTemplate`，或用 `CacheClient.set/get` 配合。为简单起见，`ProductServiceImpl` 额外注入 `StringRedisTemplate`：

```java
import org.springframework.data.redis.core.StringRedisTemplate;

// 构造函数增加参数
private final StringRedisTemplate stringRedisTemplate;
```

- [ ] **Step 3: 缓存失效 — 管理端写操作删除缓存**

```java
// ProductController.java — add 方法最后删除相关缓存
// 注入 RedisUtil：
private final RedisUtil redisUtil;
// add/edit/delete/toggleStatus 操作后：
redisUtil.del(CACHE_HOME_KEY);  // 首页数据失效
// delete/toggleStatus 后：
redisUtil.del(CACHE_PRODUCT_KEY + id);  // 商品详情缓存失效

// CategoryController.java — add/edit/delete 操作后：
redisUtil.del(CACHE_CATEGORY_TREE_KEY);  // 分类树失效
redisUtil.del(CACHE_HOME_KEY);  // 首页数据失效
```

CategoryController 需要注入 `RedisUtil`：

```java
private final RedisUtil redisUtil;

public CategoryController(CategoryService categoryService, RedisUtil redisUtil) {
    this.categoryService = categoryService;
    this.redisUtil = redisUtil;
}
```

- [ ] **Step 4: 需要为 CategoryServiceImpl 增加 StringRedisTemplate**

或者直接在 `ProductServiceImpl` 中处理分类树缓存的失效，通过 `RedisUtil.del(CACHE_CATEGORY_TREE_KEY)` 在 admin CategoryController 中完成。

- [ ] **Step 5: 运行测试**

```bash
cd mall-server && mvn test
```

---

### Task 7: 接口限流 — 注解与拦截器

**Files:**
- Create: `mall-server/src/main/java/com/qiujie/annotation/RateLimit.java`
- Create: `mall-server/src/main/java/com/qiujie/interceptor/RateLimitInterceptor.java`
- Create: `mall-server/src/main/java/com/qiujie/config/WebMvcConfig.java`
- Modify: `mall-server/src/main/java/com/qiujie/enums/BusinessStatusEnum.java`

- [ ] **Step 1: 创建 @RateLimit 注解**

```java
// annotation/RateLimit.java
package com.qiujie.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** Redis key 前缀 */
    String key();
    /** 时间窗口内允许的最大请求次数 */
    int limit() default 5;
    /** 时间窗口（秒） */
    int window() default 60;
    /** 限流提示信息 */
    String message() default "请求过于频繁，请稍后再试";
}
```

- [ ] **Step 2: 创建 RateLimitInterceptor**

```java
// interceptor/RateLimitInterceptor.java
package com.qiujie.interceptor;

import com.qiujie.annotation.RateLimit;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;

    public RateLimitInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }
        String clientIp = getClientIp(request);
        String redisKey = rateLimit.key() + clientIp;
        long current = redisUtil.increment(redisKey, 1L);
        if (current == 1) {
            redisUtil.expire(redisKey, rateLimit.window());
        }
        if (current > rateLimit.limit()) {
            throw new ServiceException(BusinessStatusEnum.RATE_LIMIT_EXCEEDED.getCode(), rateLimit.message());
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }
}
```

- [ ] **Step 3: 创建 WebMvcConfig 注册拦截器**

```java
// config/WebMvcConfig.java
package com.qiujie.config;

import com.qiujie.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor);
    }
}
```

- [ ] **Step 4: BusinessStatusEnum 增加限流错误码**

```java
// BusinessStatusEnum.java 新增
RATE_LIMIT_EXCEEDED(1700, "请求过于频繁，请稍后再试"),
```

- [ ] **Step 5: 在 Controller 方法上添加 @RateLimit 注解**

```java
// portal/AuthController.java
@RateLimit(key = "rate:login:portal:", limit = 5, window = 60, message = "登录过于频繁，请1分钟后再试")
@PostMapping("/login")
public ResponseDTO<Map<String, Object>> login(...)

@RateLimit(key = "rate:register:", limit = 3, window = 60, message = "注册过于频繁，请1分钟后再试")
@PostMapping("/register")
public ResponseDTO<Void> register(...)

@RateLimit(key = "rate:captcha:portal:", limit = 1, window = 60, message = "验证码获取过于频繁，请1分钟后再试")
@GetMapping("/verificationCode")
public ResponseDTO<Map<String, String>> getVerificationCode(...)

// admin/AuthController.java
@RateLimit(key = "rate:login:admin:", limit = 5, window = 60)
@PostMapping("/login")

@RateLimit(key = "rate:captcha:admin:", limit = 1, window = 60)
@GetMapping("/verificationCode")
```

- [ ] **Step 6: 处理拦截器抛出 ServiceException 的全局异常捕获**

项目的全局异常处理器需确认能捕获 `ServiceException` 并返回 JSON。检查 `mall-server/src/main/java/com/qiujie/handler/` 下的全局异常处理器，确认 `ServiceException` 已被处理。

- [ ] **Step 7: 运行测试**

```bash
cd mall-server && mvn test
```

---

### Task 8: 秒杀后端 — 数据层

**Files:**
- Create: `mall-server/src/main/java/com/qiujie/entity/SeckillSession.java`
- Create: `mall-server/src/main/java/com/qiujie/mapper/SeckillSessionMapper.java`
- Create: `mall-server/src/main/resources/mapper/SeckillSessionMapper.xml`
- Create: `mall-server/src/main/java/com/qiujie/enums/SeckillResultEnum.java`
- Modify: `docs/mall.sql`
- Modify: `mall-server/src/main/java/com/qiujie/enums/BusinessStatusEnum.java`

- [ ] **Step 1: 创建 SeckillSession 实体**

```java
// entity/SeckillSession.java
package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sms_seckill_session")
public class SeckillSession {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer productId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建 SeckillSessionMapper**

```java
// mapper/SeckillSessionMapper.java
package com.qiujie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiujie.entity.SeckillSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeckillSessionMapper extends BaseMapper<SeckillSession> {
    List<SeckillSession> selectActiveSessions(@Param("now") java.time.LocalDateTime now);
    List<SeckillSession> selectUpcomingSessions(@Param("now") java.time.LocalDateTime now);
}
```

- [ ] **Step 3: 创建 XML 映射**

```xml
<!-- resources/mapper/SeckillSessionMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.qiujie.mapper.SeckillSessionMapper">
    <select id="selectActiveSessions" resultType="com.qiujie.entity.SeckillSession">
        SELECT * FROM sms_seckill_session
        WHERE start_time &lt;= #{now} AND end_time &gt; #{now} AND deleted = 0
        ORDER BY end_time ASC
    </select>
    <select id="selectUpcomingSessions" resultType="com.qiujie.entity.SeckillSession">
        SELECT * FROM sms_seckill_session
        WHERE start_time &gt; #{now} AND deleted = 0
        ORDER BY start_time ASC
    </select>
</mapper>
```

- [ ] **Step 4: 创建 SeckillResultEnum**

```java
// enums/SeckillResultEnum.java
package com.qiujie.enums;

import lombok.Getter;

@Getter
public enum SeckillResultEnum {
    QUEUING(0, "排队中"),
    SUCCESS(1, "秒杀成功"),
    FAILED(-1, "秒杀失败");

    private final int code;
    private final String msg;

    SeckillResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
```

- [ ] **Step 5: 更新 docs/mall.sql 增加秒杀表**

```sql
-- docs/mall.sql 末尾追加
DROP TABLE IF EXISTS `sms_seckill_session`;
CREATE TABLE `sms_seckill_session` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL,
    `seckill_price` DECIMAL(10,2) NOT NULL,
    `seckill_stock` INT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试种子数据
INSERT INTO `sms_seckill_session` (`product_id`, `seckill_price`, `seckill_stock`, `start_time`, `end_time`) VALUES
(1, 29.90, 100, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR)),
(5, 39.90, 50, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR));
```

- [ ] **Step 6: BusinessStatusEnum 增加秒杀相关错误码**

```java
// BusinessStatusEnum.java 新增
SECKILL_SESSION_NOT_EXIST(1800, "秒杀活动不存在"),
SECKILL_SESSION_EXPIRED(1801, "秒杀活动已结束"),
SECKILL_STOCK_EMPTY(1802, "已抢光"),
SECKILL_DUPLICATE(1803, "您已参与过此秒杀活动"),
SECKILL_ORDER_FAILED(1804, "秒杀下单失败"),
```

---

### Task 9: 秒杀后端 — 业务层与控制器

**Files:**
- Create: `mall-server/src/main/java/com/qiujie/service/SeckillService.java`
- Create: `mall-server/src/main/java/com/qiujie/service/impl/SeckillServiceImpl.java`
- Create: `mall-server/src/main/java/com/qiujie/controller/portal/SeckillController.java`
- Modify: `mall-server/src/main/java/com/qiujie/config/SecurityConfig.java`

- [ ] **Step 1: 创建 SeckillService 接口**

```java
// service/SeckillService.java
package com.qiujie.service;

import java.util.List;
import java.util.Map;

public interface SeckillService {
    List<Map<String, Object>> getActiveSessions();
    List<Map<String, Object>> getUpcomingSessions();
    void execute(Integer sessionId, Integer userId);
    Map<String, Object> getResult(Integer sessionId, Integer userId);
}
```

- [ ] **Step 2: 创建 SeckillServiceImpl**

```java
// service/impl/SeckillServiceImpl.java
package com.qiujie.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.*;
import com.qiujie.enums.*;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.*;
import com.qiujie.service.SeckillService;
import com.qiujie.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.qiujie.constants.RedisConstants.SECKILL_STOCK_KEY;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSession>
        implements SeckillService {

    private final SeckillSessionMapper seckillSessionMapper;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final RedisUtil redisUtil;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public SeckillServiceImpl(SeckillSessionMapper seckillSessionMapper,
                               ProductMapper productMapper,
                               ProductImgMapper productImgMapper,
                               OrderMapper orderMapper,
                               OrderItemMapper orderItemMapper,
                               RedisUtil redisUtil) {
        this.seckillSessionMapper = seckillSessionMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.redisUtil = redisUtil;
    }

    @Override
    public List<Map<String, Object>> getActiveSessions() {
        List<SeckillSession> sessions = seckillSessionMapper.selectActiveSessions(LocalDateTime.now());
        return sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("sessionId", s.getId());
            m.put("productId", s.getProductId());
            Product product = productMapper.selectById(s.getProductId());
            m.put("productName", product != null ? product.getName() : "");
            ProductImg img = productImgMapper.selectFirstByProductId(s.getProductId());
            m.put("productImg", img != null ? img.getUrl() : "");
            m.put("seckillPrice", s.getSeckillPrice());
            // 优先使用 Redis 中的剩余库存
            String stockKey = SECKILL_STOCK_KEY + s.getId();
            Object stockObj = redisUtil.get(stockKey);
            int remaining;
            if (stockObj != null) {
                remaining = Integer.parseInt(stockObj.toString());
            } else {
                remaining = s.getSeckillStock();
                redisUtil.set(stockKey, String.valueOf(remaining));
            }
            m.put("remainingStock", remaining);
            m.put("startTime", s.getStartTime().toString());
            m.put("endTime", s.getEndTime().toString());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUpcomingSessions() {
        List<SeckillSession> sessions = seckillSessionMapper.selectUpcomingSessions(LocalDateTime.now());
        return sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("sessionId", s.getId());
            m.put("productId", s.getProductId());
            Product product = productMapper.selectById(s.getProductId());
            m.put("productName", product != null ? product.getName() : "");
            ProductImg img = productImgMapper.selectFirstByProductId(s.getProductId());
            m.put("productImg", img != null ? img.getUrl() : "");
            m.put("seckillPrice", s.getSeckillPrice());
            m.put("startTime", s.getStartTime().toString());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public void execute(Integer sessionId, Integer userId) {
        // 1. 校验场次有效性
        SeckillSession session = getById(sessionId);
        if (session == null || session.getDeleted() != null && session.getDeleted() == 1) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }

        // 2. 防重复下单
        String orderKey = "seckill:order:" + sessionId + ":" + userId;
        if (redisUtil.hasKey(orderKey)) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_DUPLICATE);
        }

        // 3. 扣库存
        String stockKey = SECKILL_STOCK_KEY + sessionId;
        Long stock = redisUtil.increment(stockKey, -1L);
        if (stock < 0) {
            redisUtil.increment(stockKey, 1L); // 回滚
            throw new ServiceException(BusinessStatusEnum.SECKILL_STOCK_EMPTY);
        }

        // 4. 标记用户已下单
        redisUtil.set(orderKey, "1");

        // 5. 异步创建订单
        String resultKey = "seckill:result:" + sessionId + ":" + userId;
        redisUtil.set(resultKey,
                JSONUtil.toJsonStr(Map.of("status", 0, "msg", "排队中")));

        EXECUTOR.submit(() -> {
            try {
                Order order = createSeckillOrder(session, userId);
                Map<String, Object> successResult = new HashMap<>();
                successResult.put("status", 1);
                successResult.put("msg", "恭喜您抢到商品！订单号：" + order.getOrderSn());
                redisUtil.set(resultKey, JSONUtil.toJsonStr(successResult), 120);
                // 同步 DB 库存
                session.setSeckillStock((int) (stock - 1));
                updateById(session);
            } catch (Exception e) {
                redisUtil.increment(stockKey, 1L); // 回滚库存
                redisUtil.del(orderKey);
                Map<String, Object> failResult = new HashMap<>();
                failResult.put("status", -1);
                failResult.put("msg", "下单失败：" + e.getMessage());
                redisUtil.set(resultKey, JSONUtil.toJsonStr(failResult), 120);
            }
        });
    }

    @Override
    public Map<String, Object> getResult(Integer sessionId, Integer userId) {
        String resultKey = "seckill:result:" + sessionId + ":" + userId;
        Object json = redisUtil.get(resultKey);
        if (json == null) {
            return Map.of("status", -1, "msg", "未找到结果");
        }
        return JSONUtil.toBean(json.toString(), Map.class);
    }

    private Order createSeckillOrder(SeckillSession session, Integer userId) {
        Product product = productMapper.selectById(session.getProductId());
        if (product == null || product.getStock() < 1) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT);
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderSn(generateOrderSn());
        order.setTotalAmount(session.getSeckillPrice());
        order.setPayMethod(PayMethodEnum.UNKNOWN);
        order.setStatus(OrderStatusEnum.PENDING_PAY);
        order.setRecipientName("");
        order.setRecipientPhone("");
        order.setRecipientAddress("");
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductPrice(session.getSeckillPrice());
        item.setAmount(1);
        ProductImg img = productImgMapper.selectFirstByProductId(product.getId());
        if (img != null) item.setProductImg(img.getUrl());
        orderItemMapper.insert(item);

        product.setStock(product.getStock() - 1);
        productMapper.updateById(product);

        return order;
    }

    private String generateOrderSn() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000));
    }
}
```

- [ ] **Step 3: 创建 SeckillController**

```java
// controller/portal/SeckillController.java
package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.SeckillService;
import com.qiujie.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/seckill")
public class SeckillController {

    private final SeckillService seckillService;
    private final JwtUtil jwtUtil;

    public SeckillController(SeckillService seckillService, JwtUtil jwtUtil) {
        this.seckillService = seckillService;
        this.jwtUtil = jwtUtil;
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

    @Operation(summary = "执行秒杀")
    @PostMapping("/execute")
    public ResponseDTO<Void> execute(@RequestParam Integer sessionId,
                                     @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Integer userId = jwtUtil.extractUserId(token, "/portal");
        seckillService.execute(sessionId, userId);
        return Response.ok("已加入排队");
    }

    @Operation(summary = "查询秒杀结果")
    @GetMapping("/result/{sessionId}")
    public ResponseDTO<Map<String, Object>> result(@PathVariable Integer sessionId,
                                                    @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Integer userId = jwtUtil.extractUserId(token, "/portal");
        return Response.success(seckillService.getResult(sessionId, userId));
    }
}
```

- [ ] **Step 4: SecurityConfig 放行秒杀接口**

```java
// SecurityConfig.java — 在 authorizeHttpRequests 中增加
.requestMatchers("/portal/seckill/**").authenticated()
```

秒杀接口需要认证（需要 userId），不需要额外放行。

---

### Task 10: 全量测试与验证

**Files:** 无新建，运行测试验证全部改动。

- [ ] **Step 1: 运行后端全量测试**

```bash
cd mall-server && mvn test
```

预期：所有已有测试通过，无回归。

- [ ] **Step 2: 验证码键隔离验证**

1. 启动后端：`cd mall-server && mvn spring-boot:run`
2. 启动 portal：`cd mall-portal && npm run dev`
3. 打开登录页，验证码图片正常显示
4. F12 Network 确认 `/portal/auth/verificationCode` 返回 uuid
5. 登录请求携带 uuid，登录成功

- [ ] **Step 3: JWT 黑名单验证**

1. 登录 → 记录 token
2. 访问 `/portal/cart/list` → 200
3. `POST /portal/auth/logout` → 200
4. 再次访问 `/portal/cart/list` → 401

- [ ] **Step 4: 缓存验证**

1. 访问 `/portal/product/detail/1` → 200
2. 再次访问 → 200（缓存命中，响应更快）
3. 管理端修改商品 → 再次访问 → 数据更新（缓存失效生效）

- [ ] **Step 5: 限流验证**

1. 连续调用 `/portal/auth/login` 6 次
2. 第 6 次返回限流错误

- [ ] **Step 6: 秒杀验证**

1. 数据库导入秒杀种子数据
2. 访问 `/portal/seckill/sessions` → 确认场次列表
3. 登录用户，调用 `/portal/seckill/execute?sessionId=1` → 返回"已加入排队"
4. 轮询 `/portal/seckill/result/1` → 确认结果

- [ ] **Step 7: 前端 E2E 测试（如有）**

```bash
cd mall-admin && npx playwright test
cd mall-portal && npx playwright test
```
