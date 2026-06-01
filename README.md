# mall-lite 轻量级商城后端系统

## 项目简介

mall-lite 是一个基于 Spring Boot 开发的轻量级商城后端系统，围绕商城核心业务实现用户注册登录、JWT 认证、轻量级 RBAC 权限控制、商品管理、库存扣减、订单创建与状态流转、Redis 商品详情缓存、超时订单自动取消等功能。

项目采用 Controller、Service、Mapper 分层结构，并结合统一响应、全局异常处理、参数校验、事务控制等方式提升接口规范性和业务稳定性。

## 技术栈

- Spring Boot
- MyBatis-Plus
- MySQL
- Redis
- JWT
- BCrypt
- Spring Task
- Maven
- Postman
- RabbitMQ
- JUnit 5
- SpringBootTest
- MockMvc
- Docker

## 核心功能

### 用户认证模块

- 用户注册
- 用户登录
- JWT Token 生成与校验
- 获取当前登录用户信息
- LoginInterceptor 统一校验 Token
- ThreadLocal 保存当前登录用户信息

### 权限控制模块

- 用户、角色、权限关系表设计
- 自定义 @RequirePermission 注解
- 普通用户与管理员接口隔离

### 商品模块

- 商品分页查询
- 商品详情查询
- 商品新增、修改、下架
- Redis 商品详情缓存
- 商品缓存失效处理

### 订单模块

- 创建订单
- 查询订单详情
- 查询我的订单
- 取消订单
- 模拟支付
- 管理员发货
- 用户确认收货
- 超时订单自动取消

## 项目亮点

1. 使用 JWT + 拦截器实现无状态登录认证。
2. 使用 ThreadLocal 保存当前登录用户信息，并在请求结束后及时清理，避免线程复用导致用户信息污染。
3. 基于自定义 @RequirePermission 注解实现轻量级 RBAC 权限控制。
4. 使用 Redis Cache Aside 模式优化商品详情查询。
5. 使用 MyBatis-Plus 条件更新控制库存扣减，避免库存被扣成负数。
6. 使用 Spring Task 定时处理超时未支付订单，并恢复库存。
7. 使用 @Transactional 保证订单、订单明细、库存、权限等多表操作的一致性。
8. 使用统一响应、全局异常处理、参数校验提升接口规范性。
9. 引入 RabbitMQ 实现订单创建后的异步事件日志记录，使用 DirectExchange、Queue、RoutingKey 完成消息投递与消费，并在事务提交后发送消息，避免数据库事务回滚但消息已发送的问题。 
10. 使用 JUnit 5、SpringBootTest 和 MockMvc 对用户认证、商品查询、订单创建、订单支付、订单取消、我的订单分页以及 RabbitMQ 消息消费等核心流程编写基础测试用例。
## 项目启动

### 1. 创建数据库

```sql
CREATE DATABASE mall_lite DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 
```

## 接口测试

项目提供 Postman 接口集合，位于：

postman/mall-lite.postman_collection.json

本地环境变量文件位于：

postman/mall-lite-local.postman_environment.json

## 项目文档

- 数据库初始化脚本：`sql/mall_lite.sql`
- Postman 接口集合：`postman/mall-lite.postman_collection.json`
- Postman 环境变量：`postman/mall-lite-local.postman_environment.json`
- AI 工具使用说明：`docs/ai-usage.md`
- Linux 部署文档：`docs/deploy-linux.md`


## 测试说明

项目使用 JUnit 5、SpringBootTest 和 MockMvc 编写基础接口测试，覆盖用户认证、商品查询、订单创建、订单支付、订单取消、我的订单分页查询以及 RabbitMQ 订单事件日志消费等核心流程。

测试类位于：

```text
src/test/java/com/example/malllite
```

运行方式：

```bash
mvn test
```

运行 RabbitMQ 相关测试前，需要确保本地 RabbitMQ 已启动：

```bash
docker start mall-rabbitmq
```

测试覆盖内容包括：

- 用户注册
- 用户登录
- 获取当前用户
- 商品分页查询
- 商品详情查询
- 创建订单
- 订单支付
- 取消订单
- 我的订单分页查询
- RabbitMQ 订单创建事件消费