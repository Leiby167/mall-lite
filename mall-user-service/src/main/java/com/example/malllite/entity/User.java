package com.example.malllite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;// 用户ID
    private String username;   // 用户名
    private String password;   // 密码
    private String email;      // 邮箱
    private String phone;      // 手机号
    private Integer status;    // 状态（启用、禁用）
    private String createTime;
}
