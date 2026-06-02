package com.example.malllite.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionRequest {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotEmpty(message = "权限标识不能为空")
    private List<String> permissionKeys;
}