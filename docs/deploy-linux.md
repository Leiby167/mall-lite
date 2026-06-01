# mall-lite Linux 部署文档

## 1. 文档说明

本文档用于说明 `mall-lite` 轻量级商城后端系统在 Linux 环境下的基础部署流程，内容包括：

- 基础环境安装
- MySQL 数据库初始化
- Redis 启动与检查
- 项目配置修改
- Maven 打包
- Spring Boot jar 包启动
- 日志查看与进程管理
- Postman 接口验证
- 常见问题排查

当前项目主要依赖：

- JDK
- Maven
- MySQL
- Redis

如后续引入 RabbitMQ，可参考本文档中的 RabbitMQ 扩展部署部分。

---

## 2. 项目环境要求

| 环境 | 建议版本 | 说明 |
|---|---|---|
| JDK | 17 或与项目 `pom.xml` 保持一致 | 运行 Spring Boot 项目 |
| Maven | 3.6+ | 项目构建与打包 |
| MySQL | 8.x | 存储用户、商品、订单、权限等业务数据 |
| Redis | 6.x / 7.x | 商品详情缓存 |
| Git | 任意稳定版本 | 拉取项目代码 |
| Linux | Ubuntu / CentOS / Debian 均可 | 部署运行环境 |
| RabbitMQ | 3.x | 用于订单创建后的异步事件日志记录 |

> 注意：实际部署时，JDK 版本应以项目 `pom.xml` 中配置的 Java 版本为准。

---

## 3. 项目目录说明

项目核心目录如下：

```text
mall-lite
├── docs
│   ├── ai-usage.md
│   └── deploy-linux.md
├── postman
│   ├── mall-lite.postman_collection.json
│   └── mall-lite-local.postman_environment.json
├── sql
│   └── mall_lite.sql
├── src
├── pom.xml
├── README.md
├── mvnw
└── mvnw.cmd
```

重要文件说明：

| 文件 | 说明 |
|---|---|
| `sql/mall_lite.sql` | 数据库初始化脚本 |
| `postman/mall-lite.postman_collection.json` | Postman 接口集合 |
| `postman/mall-lite-local.postman_environment.json` | Postman 本地环境变量 |
| `docs/ai-usage.md` | AI 工具使用说明 |
| `docs/deploy-linux.md` | Linux 部署文档 |

---

## 4. 安装基础环境

以下命令以 Ubuntu 系统为例。  
如果使用 CentOS，需要将 `apt` 替换为 `yum` 或 `dnf`。

---

### 4.1 更新软件源

```bash
sudo apt update
```

---

### 4.2 安装 JDK

检查 Java 是否已安装：

```bash
java -version
```

如果未安装，可以安装 OpenJDK 17：

```bash
sudo apt install openjdk-17-jdk -y
```

安装完成后再次检查：

```bash
java -version
```

示例输出：

```text
openjdk version "17.x.x"
```

---

### 4.3 安装 Maven

检查 Maven 是否已安装：

```bash
mvn -v
```

如果未安装：

```bash
sudo apt install maven -y
```

安装完成后检查：

```bash
mvn -v
```

---

### 4.4 安装 Git

检查 Git：

```bash
git --version
```

如果未安装：

```bash
sudo apt install git -y
```

---

## 5. 安装并配置 MySQL

### 5.1 安装 MySQL

```bash
sudo apt install mysql-server -y
```

---

### 5.2 启动 MySQL

```bash
sudo systemctl start mysql
```

设置开机自启：

```bash
sudo systemctl enable mysql
```

查看 MySQL 状态：

```bash
sudo systemctl status mysql
```

---

### 5.3 登录 MySQL

```bash
mysql -u root -p
```

如果本地环境未设置 root 密码，也可以尝试：

```bash
sudo mysql
```

---

### 5.4 创建数据库

登录 MySQL 后执行：

```sql
CREATE DATABASE mall_lite DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

查看数据库是否创建成功：

```sql
SHOW DATABASES;
```

退出 MySQL：

```sql
exit;
```

---

### 5.5 导入初始化 SQL

进入项目根目录：

```bash
cd mall-lite
```

执行 SQL 导入：

```bash
mysql -u root -p mall_lite < sql/mall_lite.sql
```

如果使用 `sudo mysql`，可以这样导入：

```bash
sudo mysql mall_lite < sql/mall_lite.sql
```

---

### 5.6 检查表结构

登录 MySQL：

```bash
mysql -u root -p
```

执行：

```sql
USE mall_lite;
SHOW TABLES;
```

正常情况下应包含以下表：

```text
sys_user
sys_role
user_role
role_permission
product
order_info
order_item
```

检查初始化角色数据：

```sql
SELECT * FROM sys_role;
```

检查权限数据：

```sql
SELECT * FROM role_permission;
```

检查商品数据：

```sql
SELECT * FROM product;
```

---

## 6. 安装并启动 Redis

### 6.1 安装 Redis

```bash
sudo apt install redis-server -y
```

---

### 6.2 启动 Redis

```bash
sudo systemctl start redis-server
```

设置开机自启：

```bash
sudo systemctl enable redis-server
```

查看 Redis 状态：

```bash
sudo systemctl status redis-server
```

---

### 6.3 检查 Redis 是否可用

```bash
redis-cli ping
```

如果返回：

```text
PONG
```

说明 Redis 启动成功。

---

## 7. 拉取或上传项目代码

### 7.1 使用 Git 拉取代码

如果项目已上传到 Git 仓库，可以执行：

```bash
git clone 仓库地址
```

例如：

```bash
git clone https://gitee.com/yourname/mall-lite.git
```

进入项目目录：

```bash
cd mall-lite
```

---

### 7.2 手动上传项目

如果没有使用 Git，也可以将项目压缩后上传到 Linux 服务器：

```bash
scp mall-lite.zip 用户名@服务器IP:/home/用户名/
```

服务器上解压：

```bash
unzip mall-lite.zip
cd mall-lite
```

---

## 8. 修改项目配置

配置文件通常位于：

```text
src/main/resources/application.yml
```

需要重点检查以下配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall_lite?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码

  data:
    redis:
      host: localhost
      port: 6379
```

如果你的项目使用的是旧版 Redis 配置，也可能是：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

请以项目实际配置为准。

---

### 8.1 数据库配置说明

| 配置项 | 说明 |
|---|---|
| `url` | MySQL 连接地址 |
| `username` | MySQL 用户名 |
| `password` | MySQL 密码 |

如果 MySQL 和项目部署在同一台服务器：

```yaml
url: jdbc:mysql://localhost:3306/mall_lite?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
```

如果 MySQL 部署在其他服务器：

```yaml
url: jdbc:mysql://MySQL服务器IP:3306/mall_lite?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
```

---

### 8.2 Redis 配置说明

如果 Redis 和项目部署在同一台服务器：

```yaml
host: localhost
port: 6379
```

如果 Redis 部署在其他服务器：

```yaml
host: Redis服务器IP
port: 6379
```

---

## 9. 项目打包

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

打包成功后，jar 包通常位于：

```text
target/
```

查看打包结果：

```bash
ls target
```

可能生成类似文件：

```text
mall-lite-0.0.1-SNAPSHOT.jar
```

实际文件名以 `target` 目录中生成的 jar 包为准。

---

## 10. 启动项目

### 10.1 前台启动

```bash
java -jar target/mall-lite-0.0.1-SNAPSHOT.jar
```

前台启动适合临时测试。  
如果关闭终端，项目会停止。

---

### 10.2 后台启动

推荐使用 `nohup` 后台启动：

```bash
nohup java -jar target/mall-lite-0.0.1-SNAPSHOT.jar > mall-lite.log 2>&1 &
```

说明：

| 命令 | 说明 |
|---|---|
| `nohup` | 终端关闭后程序继续运行 |
| `>` | 将日志输出到文件 |
| `2>&1` | 将错误日志也输出到同一个文件 |
| `&` | 后台运行 |

---

### 10.3 查看启动日志

```bash
tail -f mall-lite.log
```

如果看到类似 Spring Boot 启动成功日志，说明项目已经正常启动。

---

## 11. 检查项目运行状态

### 11.1 查看 Java 进程

```bash
ps -ef | grep java
```

或者：

```bash
ps -ef | grep mall-lite
```

---

### 11.2 查看端口占用

如果项目端口是 8080：

```bash
lsof -i:8080
```

如果系统没有 `lsof`，可以安装：

```bash
sudo apt install lsof -y
```

也可以使用：

```bash
netstat -tunlp | grep 8080
```

如果没有 `netstat`，安装：

```bash
sudo apt install net-tools -y
```

---

## 12. 停止项目

先查看进程：

```bash
ps -ef | grep java
```

找到项目对应的进程 ID，例如：

```text
root      12345     1  0 10:00 ?        00:00:10 java -jar target/mall-lite-0.0.1-SNAPSHOT.jar
```

停止项目：

```bash
kill -9 12345
```

再次查看：

```bash
ps -ef | grep java
```

确认项目进程已停止。

---

## 13. 接口验证

项目启动成功后，可以使用 Postman 导入以下文件：

```text
postman/mall-lite.postman_collection.json
postman/mall-lite-local.postman_environment.json
```

---

### 13.1 本机测试

如果 Postman 和后端项目在同一台机器上，环境变量 `baseUrl` 设置为：

```text
http://localhost:8080
```

---

### 13.2 远程服务器测试

如果后端部署在 Linux 服务器，本地电脑使用 Postman 访问，需要将 `baseUrl` 改成服务器 IP：

```text
http://服务器IP:8080
```

例如：

```text
http://192.168.1.100:8080
```

---

### 13.3 推荐测试顺序

Postman 中推荐按以下顺序测试：

```text
1. 用户注册
2. 用户登录
3. 获取当前用户
4. 商品分页查询
5. 商品详情查询
6. 创建订单
7. 我的订单
8. 订单详情
9. 模拟支付
10. 管理员订单分页查询
11. 管理员发货
12. 确认收货
13. 取消订单
```

说明：

- 登录成功后，Postman 脚本会自动将返回的 JWT Token 写入环境变量 `token`。
- 后续接口通过 `Authorization: Bearer {{token}}` 访问。
- 管理员接口需要使用拥有管理员角色的账号登录。
- 发货接口要求订单状态为已支付。
- 确认收货接口要求订单状态为已发货。
- 取消订单接口要求订单状态为待支付。

---

## 14. 管理员账号准备说明

项目初始化 SQL 中包含角色和权限数据，但用户密码使用 BCrypt 加密，因此不建议直接通过 SQL 插入明文密码用户。

推荐方式：

1. 通过注册接口创建用户。
2. 在数据库中给该用户绑定管理员角色。

例如，先通过接口注册：

```json
{
  "username": "admin",
  "password": "123456",
  "email": "admin@qq.com",
  "phone": "13800000000"
}
```

然后查询用户 ID：

```sql
SELECT id, username FROM sys_user WHERE username = 'admin';
```

查询管理员角色：

```sql
SELECT id, role_name, role_key FROM sys_role;
```

假设：

```text
admin 用户 id = 3
管理员角色 id = 1
```

绑定管理员角色：

```sql
INSERT INTO user_role (user_id, role_id)
VALUES (3, 1);
```

如果需要避免重复插入：

```sql
INSERT IGNORE INTO user_role (user_id, role_id)
VALUES (3, 1);
```

检查绑定结果：

```sql
SELECT 
    u.id,
    u.username,
    r.role_name,
    r.role_key
FROM sys_user u
LEFT JOIN user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';
```

正常结果应包含：

```text
admin    管理员    admin
```

---

## 15. 常见问题排查

### 15.1 项目启动失败：端口被占用

查看端口占用：

```bash
lsof -i:8080
```

停止占用端口的进程：

```bash
kill -9 进程ID
```

或者修改 `application.yml` 中的端口：

```yaml
server:
  port: 8081
```

---

### 15.2 项目启动失败：数据库连接失败

检查 MySQL 是否启动：

```bash
sudo systemctl status mysql
```

检查数据库是否存在：

```bash
mysql -u root -p
```

```sql
SHOW DATABASES;
```

检查配置文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall_lite
    username: root
    password: 你的数据库密码
```

常见原因：

- MySQL 没启动
- 数据库名写错
- 用户名或密码错误
- 数据库端口不是 3306
- 服务器防火墙未开放端口
- MySQL 不允许远程连接

---

### 15.3 项目启动失败：Redis 连接失败

检查 Redis 是否启动：

```bash
sudo systemctl status redis-server
```

测试 Redis：

```bash
redis-cli ping
```

返回：

```text
PONG
```

说明 Redis 正常。

检查配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

### 15.4 SQL 导入失败

可能原因：

- 数据库不存在
- SQL 文件路径错误
- MySQL 用户权限不足
- 字符集不兼容
- 表已存在但脚本没有正确删除旧表

重新创建数据库：

```sql
DROP DATABASE IF EXISTS mall_lite;
CREATE DATABASE mall_lite DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

重新导入：

```bash
mysql -u root -p mall_lite < sql/mall_lite.sql
```

---

### 15.5 登录失败

可能原因：

- 用户不存在
- 密码错误
- 数据库存储的密码不是 BCrypt 加密格式
- 账号状态被禁用
- JWT 配置异常

建议：

- 通过注册接口创建用户，不要手动插入明文密码。
- 检查 `sys_user` 表中的用户状态。
- 检查登录接口返回信息。

---

### 15.6 接口返回用户未登录

可能原因：

- 请求未携带 Token
- Token 已过期
- Postman 环境变量未选择
- Header 格式错误

正确 Header：

```text
Authorization: Bearer {{token}}
```

注意：

```text
Bearer 和 {{token}} 中间必须有一个空格
```

---

### 15.7 接口返回无权限

可能原因：

- 当前用户没有管理员角色
- 用户角色未绑定
- 角色权限数据缺失
- `permission_key` 与代码中的 `@RequirePermission` 不一致

检查用户角色：

```sql
SELECT 
    u.id,
    u.username,
    r.role_name,
    r.role_key
FROM sys_user u
LEFT JOIN user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';
```

检查角色权限：

```sql
SELECT *
FROM role_permission
WHERE role_id = 1;
```

---

### 15.8 jar 包启动后立即退出

查看日志：

```bash
tail -f mall-lite.log
```

重点检查：

- 数据库连接
- Redis 连接
- 端口占用
- 配置文件格式
- 依赖是否缺失
- SQL 表是否初始化完成

---

## 16. 防火墙与端口开放

如果项目部署在云服务器上，本地无法访问接口，需要检查防火墙和云服务器安全组。

---

### 16.1 Ubuntu 防火墙

查看防火墙状态：

```bash
sudo ufw status
```

开放 8080 端口：

```bash
sudo ufw allow 8080
```

重新加载：

```bash
sudo ufw reload
```

---

### 16.2 云服务器安全组

如果使用云服务器，还需要在云平台控制台开放端口：

```text
8080
```

协议选择：

```text
TCP
```

授权对象可以先设置为自己的公网 IP，测试阶段也可临时开放：

```text
0.0.0.0/0
```

正式环境不建议长期开放过大范围。

---

## 17. 可选：RabbitMQ 扩展部署

当前版本如果尚未接入 RabbitMQ，可以暂时跳过本节。  
后续如果引入 RabbitMQ 订单异步消息，可按以下步骤部署。

---

### 17.1 安装 RabbitMQ

Ubuntu 安装：

```bash
sudo apt install rabbitmq-server -y
```

启动 RabbitMQ：

```bash
sudo systemctl start rabbitmq-server
```

设置开机自启：

```bash
sudo systemctl enable rabbitmq-server
```

查看状态：

```bash
sudo systemctl status rabbitmq-server
```

---

### 17.2 启用管理后台

```bash
sudo rabbitmq-plugins enable rabbitmq_management
```

RabbitMQ 管理后台默认端口：

```text
15672
```

浏览器访问：

```text
http://服务器IP:15672
```

默认账号密码：

```text
guest / guest
```

注意：默认 `guest` 用户通常只允许本机登录。远程访问建议创建新用户。

---

### 17.3 创建 RabbitMQ 用户

```bash
sudo rabbitmqctl add_user mall_user mall_password
```

设置为管理员：

```bash
sudo rabbitmqctl set_user_tags mall_user administrator
```

授权：

```bash
sudo rabbitmqctl set_permissions -p / mall_user ".*" ".*" ".*"
```

---

### 17.4 项目配置示例

后续接入 RabbitMQ 后，可在 `application.yml` 中添加：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: mall_user
    password: mall_password
    virtual-host: /
```