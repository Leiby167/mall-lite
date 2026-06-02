package com.example.malllite.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleAddRequest {
    @NotBlank
    private String roleName;

    @NotBlank
    private String roleKey;

    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status;
}
