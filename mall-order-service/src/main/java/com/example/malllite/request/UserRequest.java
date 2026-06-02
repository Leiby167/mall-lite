package com.example.malllite.request;

import lombok.Data;

@Data
public class UserRequest {
    private Long id;           // 用户ID
    private String username;   // 用户名
    private String password;   // 密码
    private String email;      // 邮箱
    private String phone;      // 手机号
    private Integer status;    // 用户状态（启用、禁用）

}
