DROP TABLE IF EXISTS order_event_log;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS order_info;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;


CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(100) NOT NULL COMMENT '密码',
                          email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                          phone VARCHAR(15) DEFAULT NULL COMMENT '手机号',
                          status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          UNIQUE KEY uk_username (username)
) COMMENT='用户表';

CREATE TABLE sys_role (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
                          role_key VARCHAR(50) NOT NULL COMMENT '角色标识',
                          status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY uk_role_key (role_key)
) COMMENT='角色表';

CREATE TABLE user_role (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL COMMENT '用户ID',
                           role_id BIGINT NOT NULL COMMENT '角色ID',
                           UNIQUE KEY uk_user_role (user_id, role_id),
                           KEY idx_user_id (user_id),
                           KEY idx_role_id (role_id)
) COMMENT='用户角色关联表';

CREATE TABLE role_permission (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 role_id BIGINT NOT NULL COMMENT '角色ID',
                                 permission_key VARCHAR(100) NOT NULL COMMENT '权限标识',
                                 permission_name VARCHAR(100) DEFAULT NULL COMMENT '权限名称',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE KEY uk_role_permission (role_id, permission_key),
                                 KEY idx_role_id (role_id),
                                 KEY idx_permission_key (permission_key)
) COMMENT='角色权限关联表';

CREATE TABLE product (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
                         price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
                         stock INT NOT NULL DEFAULT 0 COMMENT '库存',
                         status TINYINT DEFAULT 1 COMMENT '状态：1上架，0下架',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         KEY idx_product_name (product_name),
                         KEY idx_status (status)
) COMMENT='商品表';

CREATE TABLE order_info (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
                            user_id BIGINT NOT NULL COMMENT '用户ID',
                            total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
                            status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付，1已支付，2已发货，3已完成，4已取消',
                            pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
                            ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
                            complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
                            cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UNIQUE KEY uk_order_no (order_no),
                            KEY idx_user_id (user_id),
                            KEY idx_status (status),
                            KEY idx_create_time (create_time)
) COMMENT='订单主表';

CREATE TABLE order_item (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            order_id BIGINT NOT NULL COMMENT '订单ID',
                            product_id BIGINT NOT NULL COMMENT '商品ID',
                            product_name VARCHAR(100) NOT NULL COMMENT '商品名称快照',
                            product_price DECIMAL(10, 2) NOT NULL COMMENT '商品价格快照',
                            quantity INT NOT NULL COMMENT '购买数量',
                            total_price DECIMAL(10, 2) NOT NULL COMMENT '明细总价',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                            KEY idx_order_id (order_id),
                            KEY idx_product_id (product_id)
) COMMENT='订单明细表';

CREATE TABLE order_event_log (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 order_id BIGINT NOT NULL COMMENT '订单ID',
                                 event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
                                 event_content VARCHAR(500) DEFAULT NULL COMMENT '事件内容',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 KEY idx_order_id (order_id),
                                 KEY idx_event_type (event_type)
) COMMENT='订单事件日志表';

INSERT INTO sys_role (id, role_name, role_key, status)
VALUES
    (1, '管理员', 'admin', 1),
    (2, '普通用户', 'user', 1);

INSERT INTO role_permission (role_id, permission_key, permission_name)
VALUES
    (1, 'role:query', '查询角色'),
    (1, 'role:add', '新增角色'),
    (1, 'role:update', '修改角色'),
    (1, 'role:delete', '删除角色'),
    (1, 'role:assign-permissions', '分配角色权限'),

    (1, 'order:query', '查看订单'),
    (1, 'order:ship', '订单发货'),

    (1, 'product:add', '新增商品'),
    (1, 'product:update', '修改商品'),
    (1, 'product:delete', '删除或下架商品'),

    (1, 'user:query', '查询用户'),
    (1, 'user:add', '新增用户'),
    (1, 'user:update', '修改用户'),
    (1, 'user:delete', '删除用户'),
    (1, 'user:assign-role', '分配用户角色');

INSERT INTO product (id, product_name, price, stock, status)
VALUES
    (1, '无线鼠标', 59.90, 100, 1),
    (2, '机械键盘', 199.00, 50, 1),
    (3, '保温杯', 39.90, 80, 1);