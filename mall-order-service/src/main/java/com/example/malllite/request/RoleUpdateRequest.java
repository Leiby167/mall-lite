package com.example.malllite.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class RoleUpdateRequest {
    @NotNull(message = "id不能为空")
    private Long id;

    @NotBlank(message = "角色名不能为空")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    private String roleKey;

    @Min(value = 0, message = "角色状态只能是0或1")
    @Max(value = 1, message = "角色状态只能是0或1")
    private Integer status;
}
