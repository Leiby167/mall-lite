package com.example.malllite.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleDeleteRequest {
    @NotNull(message = "角色id不能为空")
    private Long id;
}
