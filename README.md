# mall-lite-cloud 商城微服务项目

## 1. 项目简介

`mall-lite-cloud` 是基于 `mall-lite` 单体商城后端系统演进而来的 Spring Cloud Alibaba 微服务版本。

项目将原有单体商城系统按照业务边界拆分为公共模块、网关模块、用户服务、商品服务和订单服务，并使用 Nacos、Spring Cloud Gateway、OpenFeign 完成服务注册、统一入口和服务间调用。

该分支主要用于展示从 Spring Boot 单体项目到 Spring Cloud Alibaba 微服务架构的基础改造过程。

---

## 2. 分支说明

| 分支 | 说明 |
| --- | --- |
| `main` | Spring Boot 单体版商城后端系统 |
| `mall-lite-cloud` | Spring Cloud Alibaba 微服务改造版 |

---

## 3. 技术栈

- Spring Boot 4
- Spring Cloud Alibaba
- Nacos
- Spring Cloud Gateway
- OpenFeign
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- JWT
- Maven 多模块
- Docker
- Postman

---

## 4. 模块划分

```text
mall-lite-cloud
├── mall-common
├── mall-gateway
├── mall-user-service
├── mall-product-service
├── mall-order-service
├── mall-single-backup
├── docs
├── postman
├── sql
└── README.md
```


模块	                       说明
mall-common	               公共模块，包含统一响应、异常处理、Token 工具、登录用户上下文、公共 DTO
mall-gateway	           网关模块，作为统一入口，负责路由转发
mall-user-service	       用户服务，负责注册、登录、当前用户、角色权限相关接口
mall-product-service	   商品服务，负责商品分页、商品详情、Redis 缓存、库存扣减、库存恢复
mall-order-service	       订单服务，负责创建订单、支付、取消、发货、确认收货、订单事件日志
mall-single-backup	       单体项目备份，保留原始单体版代码，便于对比微服务改造前后差异




5. 核心功能
   5.1 用户服务
   用户注册
   用户登录
   JWT 登录认证
   当前用户查询
   角色权限管理
   登录用户上下文维护
   5.2 商品服务
   商品分页查询
   商品详情查询
   Redis 商品详情缓存
   商品库存扣减
   商品库存恢复
   提供内部商品查询与库存接口，供订单服务远程调用
   5.3 订单服务
   创建订单
   我的订单查询
   订单详情查询
   模拟支付
   取消订单
   管理员发货
   用户确认收货
   超时订单自动取消
   RabbitMQ 异步记录订单事件日志
   5.4 网关服务
   /user/** 转发到用户服务
   /product/** 转发到商品服务
   /order/** 转发到订单服务
   对外提供统一访问入口，降低客户端直接访问多个服务端口的复杂度

6. 微服务调用链路
   6.1 整体访问链路
   客户端
   ↓
   mall-gateway
   ↓
   mall-user-service / mall-product-service / mall-order-service
   6.2 订单创建链路
   客户端提交订单
   ↓
   mall-order-service 创建订单
   ↓
   OpenFeign 调用 mall-product-service 查询商品信息
   ↓
   OpenFeign 调用 mall-product-service 扣减库存
   ↓
   保存订单和订单明细
   ↓
   RabbitMQ 异步记录订单事件日志
   6.3 订单取消链路
   用户取消订单 / 定时任务取消超时订单
   ↓
   mall-order-service 更新订单状态
   ↓
   OpenFeign 调用 mall-product-service 恢复库存

7. 启动前准备

本项目依赖以下中间件：

MySQL
Redis
RabbitMQ
Nacos
7.1 Docker 启动中间件
docker start mall-nacos
docker start mall-redis
docker start mall-rabbitmq
7.2 Nacos 控制台
http://localhost:8848/nacos

默认账号密码：

nacos / nacos
7.3 RabbitMQ 控制台
http://localhost:15672

默认账号密码：

guest / guest

8. 服务启动顺序

建议按以下顺序启动：

1. Nacos
2. Redis
3. RabbitMQ
4. MySQL
5. mall-user-service
6. mall-product-service
7. mall-order-service
8. mall-gateway

对应启动类：

服务	启动类
mall-user-service	MallUserServiceApplication
mall-product-service	MallProductServiceApplication
mall-order-service	MallOrderServiceApplication
mall-gateway	MallGatewayApplication

9. 服务端口
   服务	端口
   mall-gateway	8080
   mall-user-service	8101
   mall-product-service	8102
   mall-order-service	8103
   Nacos	8848
   RabbitMQ Management	15672
   Redis	6379

10. 配置说明

各服务配置文件位于对应模块的：

src/main/resources/application.properties
10.1 MySQL 配置

需要根据本地环境修改数据库账号和密码：

spring.datasource.url=jdbc:mysql://localhost:3306/mall_lite?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的数据库密码
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
10.2 Nacos 配置
spring.cloud.nacos.discovery.server-addr=localhost:8848
10.3 Redis 配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
10.4 RabbitMQ 配置
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

11. 接口测试

推荐通过 Gateway 统一访问：

http://localhost:8080

也可以直接访问各服务端口：

用户服务：http://localhost:8101
商品服务：http://localhost:8102
订单服务：http://localhost:8103
网关入口：http://localhost:8080
11.1 用户登录
POST http://localhost:8080/user/login

登录成功后，请求头携带：

Authorization: Bearer {{token}}
11.2 商品接口
GET http://localhost:8080/product/page?current=1&size=10
GET http://localhost:8080/product/1
11.3 订单接口
POST http://localhost:8080/order/create
GET  http://localhost:8080/order/my/page?pageNum=1&pageSize=10
GET  http://localhost:8080/order/{id}
POST http://localhost:8080/order/pay/{id}
POST http://localhost:8080/order/cancel/{id}
POST http://localhost:8080/order/ship/{id}
POST http://localhost:8080/order/complete/{id}

创建订单示例请求体：

{
"items": [
{
"productId": 1,
"quantity": 1
}
]
}


12. 编译方式

在项目根目录执行：

mvnw.cmd clean compile

单独编译某个模块：

mvnw.cmd clean compile -pl mall-user-service -am
mvnw.cmd clean compile -pl mall-product-service -am
mvnw.cmd clean compile -pl mall-order-service -am


13. 项目亮点
    基于 Maven 多模块完成单体项目微服务化拆分
    使用 Nacos 实现服务注册与发现
    使用 Spring Cloud Gateway 统一转发用户、商品、订单请求
    使用 OpenFeign 实现订单服务远程调用商品服务
    订单创建时通过远程调用完成商品查询和库存扣减
    订单取消和超时取消时通过远程调用恢复库存
    保留 Redis 商品详情缓存能力，降低数据库查询压力
    保留 RabbitMQ 订单事件异步处理能力，降低下单主流程耦合
    保留 JWT 登录认证与 ThreadLocal 用户上下文能力
    使用 Git 分支隔离单体版和微服务版，降低改造风险


14. 项目说明

Spring Boot 后端开发能力
MySQL、Redis、RabbitMQ 使用能力
Spring Cloud Alibaba 微服务基础实践能力
单体项目向微服务架构演进的工程化思路