# 模块完成报告

## 一、Spring Boot 3 升级与项目重构

- Spring Boot 2.5.6 → 3.2.5, javax.* → jakarta.* 全量迁移
- Swagger 2 → springdoc-openapi 2.3.0, jjwt 0.11.5 → 0.12.5
- sys_staff → sys_user 表与实体重命名, Staff 系列统一重命名为 User
- oms_order_info → oms_order, 移除 dept_id 部门相关功能
- @Autowired 字段注入 → 构造器注入, Spring Security 6 lambda DSL
- 相关文件: pom.xml, SecurityConfig.java, JwtUtil.java, entity/*, service/*, controller/*

## 二、用户模块

- 门户用户注册/登录 (/api/auth/register, /api/auth/login, 返回 JWT)
- 管理后台用户管理 (/user CRUD, 角色分配) 和登录 (/login)
- 后台管理员与门户用户共用 sys_user 表, 密码 BCrypt 加密
- 相关文件: User.java, UserMapper.java, AuthController.java, UserController.java

## 三、商品模块

- 首页分类+商品聚合, 两级分类树, 按分类分页查询含子分类商品
- MySQL LIKE 关键词搜索 (后续替换为 Elasticsearch)
- 商品详情含分类名称和图片列表 (LEFT JOIN + resultMap 嵌套映射)
- 相关文件: Category.java, Product.java, ProductMapper.java+XML, ProductController.java

## 四、购物车模块

- 购物车列表/加入/修改数量/选中状态/删除单项/批量删除
- LEFT JOIN 关联商品和图片, 加入时累加数量, 通过 JWT userId 隔离数据
- 相关文件: Cart.java, CartMapper.java+XML, CartService.java, CartController.java

## 五、订单模块

- 购物车下单 (库存校验→扣减→创建订单→清空购物车), @Version 乐观锁防超卖
- 模拟支付, 订单列表/详情 (嵌套 collection 映射明细), 逻辑删除
- 订单状态: 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消
- 相关文件: Order.java, OrderItem.java, OrderMapper.java+XML, OrderService.java

## 六、管理后台升级

- Vue 2/Element UI/Vue CLI/Vuex → Vue 3/Element Plus/Vite/Pinia
- 登录/侧边栏导航/标签页/用户管理/角色管理/菜单管理/仪表板/404
- Composition API (<script setup>), v-permission 指令适配 Vue 3
- 新增分类管理、商品管理、订单管理页面及对应 API
- 相关文件: mall-admin/src/ (全部重构), HomeController.java

## 七、OSS 文件存储集成

- 阿里云 OSS Java SDK, OssConfig + OssService (上传/删除/签名URL)
- POST /api/common/upload 统一上传接口, 前端 el-upload 集成
- 种子数据 docs/mall-data.sql: 5 个一级分类 + 16 个二级分类, 18 件商品 56 张图片
- 敏感配置通过环境变量注入 (OSS_ENDPOINT, OSS_ACCESS_KEY_ID 等)
- 相关文件: OssConfig.java, OssService.java, CommonController.java, application-dev.yml
