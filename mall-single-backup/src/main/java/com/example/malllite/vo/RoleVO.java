package com.example.malllite.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleVO {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
}
