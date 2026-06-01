package com.example.malllite.controller;

import com.example.malllite.annotation.RequirePermission;
import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.PageResponse;
import com.example.malllite.common.ResultUtils;
import com.example.malllite.request.RoleAddRequest;
import com.example.malllite.request.RoleDeleteRequest;
import com.example.malllite.request.RolePermissionRequest;
import com.example.malllite.request.RoleQueryRequest;
import com.example.malllite.request.RoleUpdateRequest;
import com.example.malllite.service.RoleService;
import com.example.malllite.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @RequirePermission(permissionKey = "role:query")
    @GetMapping("/{id}")
    public BaseResponse<RoleVO> getRoleById(@PathVariable Long id) {
        return ResultUtils.success(roleService.getRoleById(id));
    }

    @RequirePermission(permissionKey = "role:add")
    @PostMapping("/add")
    public BaseResponse<Boolean> addRole(@RequestBody @Valid RoleAddRequest request) {
        return ResultUtils.success(roleService.addRole(request));
    }

    @RequirePermission(permissionKey = "role:query")
    @GetMapping("/page")
    public BaseResponse<PageResponse<RoleVO>> pageRole(@Valid RoleQueryRequest request) {
        return ResultUtils.success(roleService.pageRole(request));
    }

    @RequirePermission(permissionKey = "role:update")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateRole(@RequestBody @Valid RoleUpdateRequest request) {
        return ResultUtils.success(roleService.updateRole(request));
    }

    @RequirePermission(permissionKey = "role:delete")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteRole(@RequestBody @Valid RoleDeleteRequest request) {
        return ResultUtils.success(roleService.deleteRole(request));
    }

    @RequirePermission(permissionKey = "role:assign-permissions")
    @PostMapping("/assign-permissions")
    public BaseResponse<Boolean> assignPermissions(@RequestBody @Valid RolePermissionRequest request) {
        return ResultUtils.success(roleService.assignPermissions(request));
    }
}