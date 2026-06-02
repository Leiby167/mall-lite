package com.example.malllite.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RoleQueryRequest {
    @Min(value = 1, message = "页码必须大于等于 1")
    private long pageNum = 1;
    @Min(value = 1,message = "每页大小必须大于等于 1")
    private long pageSize = 10;
    private String roleName;
    private Integer status;
}
