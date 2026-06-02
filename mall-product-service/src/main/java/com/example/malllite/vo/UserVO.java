package com.example.malllite.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer status;

    private Long roleId;
    private String roleName;
    private String roleKey;

}
