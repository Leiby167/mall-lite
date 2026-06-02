package com.example.malllite.service;

import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.PageResponse;
import com.example.malllite.request.RoleAddRequest;
import com.example.malllite.request.RoleDeleteRequest;
import com.example.malllite.request.RolePermissionRequest;
import com.example.malllite.request.RoleQueryRequest;
import com.example.malllite.request.RoleUpdateRequest;
import com.example.malllite.vo.RoleVO;

public interface RoleService {

    RoleVO getRoleById(Long id);

    Boolean addRole(RoleAddRequest request);

    PageResponse<RoleVO> pageRole(RoleQueryRequest request);

    Boolean updateRole(RoleUpdateRequest request);

    Boolean deleteRole(RoleDeleteRequest request);

    Boolean assignPermissions(RolePermissionRequest request);
}