package com.example.malllite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.PageResponse;
import com.example.malllite.entity.Role;
import com.example.malllite.entity.RolePermission;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.mapper.RoleMapper;
import com.example.malllite.mapper.RolePermissionMapper;
import com.example.malllite.request.RoleAddRequest;
import com.example.malllite.request.RoleDeleteRequest;
import com.example.malllite.request.RolePermissionRequest;
import com.example.malllite.request.RoleQueryRequest;
import com.example.malllite.request.RoleUpdateRequest;
import com.example.malllite.service.RoleService;
import com.example.malllite.vo.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    private final RolePermissionMapper rolePermissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper,
                           RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public RoleVO getRoleById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色ID不合法");
        }

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }

        return toRoleVO(role);
    }

    @Override
    public Boolean addRole(RoleAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        if (!StringUtils.hasText(request.getRoleName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色名称不能为空");
        }

        if (!StringUtils.hasText(request.getRoleKey())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识不能为空");
        }

        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleKey, request.getRoleKey().trim());

        Long count = roleMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识已存在");
        }

        Role role = new Role();
        role.setRoleName(request.getRoleName().trim());
        role.setRoleKey(request.getRoleKey().trim());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        int rows = roleMapper.insert(role);
        return rows > 0;
    }

    @Override
    public PageResponse<RoleVO> pageRole(RoleQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getRoleName())) {
            queryWrapper.like(Role::getRoleName, request.getRoleName().trim());
        }

        if (request.getStatus() != null) {
            queryWrapper.eq(Role::getStatus, request.getStatus());
        }

        queryWrapper.orderByDesc(Role::getId);

        Page<Role> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Role> rolePage = roleMapper.selectPage(page, queryWrapper);

        List<RoleVO> roleVOList = new ArrayList<>();
        if (rolePage.getRecords() != null) {
            for (Role role : rolePage.getRecords()) {
                roleVOList.add(toRoleVO(role));
            }
        }

        return new PageResponse<>(
                roleVOList,
                rolePage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public Boolean updateRole(RoleUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        }

        if (!StringUtils.hasText(request.getRoleName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色名称不能为空");
        }

        if (!StringUtils.hasText(request.getRoleKey())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识不能为空");
        }

        Role oldRole = roleMapper.selectById(request.getId());
        if (oldRole == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }

        String newRoleKey = request.getRoleKey().trim();

        if (!newRoleKey.equals(oldRole.getRoleKey())) {
            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Role::getRoleKey, newRoleKey);

            Long count = roleMapper.selectCount(queryWrapper);
            if (count != null && count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识已存在");
            }
        }

        Role role = new Role();
        role.setId(request.getId());
        role.setRoleName(request.getRoleName().trim());
        role.setRoleKey(newRoleKey);
        role.setStatus(request.getStatus() == null ? oldRole.getStatus() : request.getStatus());

        int rows = roleMapper.updateById(role);
        return rows > 0;
    }

    @Override
    @Transactional
    public Boolean deleteRole(RoleDeleteRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        }

        Role role = roleMapper.selectById(request.getId());
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }

        LambdaQueryWrapper<RolePermission> permissionQueryWrapper = new LambdaQueryWrapper<>();
        permissionQueryWrapper.eq(RolePermission::getRoleId, request.getId());
        rolePermissionMapper.delete(permissionQueryWrapper);

        int rows = roleMapper.deleteById(request.getId());
        return rows > 0;
    }

    @Override
    @Transactional
    public Boolean assignPermissions(RolePermissionRequest request) {
        if (request == null || request.getRoleId() == null || request.getRoleId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        }

        if (request.getPermissionKeys() == null || request.getPermissionKeys().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限标识不能为空");
        }

        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }

        if (role.getStatus() != null && role.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色已被禁用");
        }

        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, request.getRoleId());
        rolePermissionMapper.delete(queryWrapper);

        for (String permissionKey : request.getPermissionKeys()) {
            if (!StringUtils.hasText(permissionKey)) {
                continue;
            }

            String trimmedPermissionKey = permissionKey.trim();

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(request.getRoleId());
            rolePermission.setPermissionKey(trimmedPermissionKey);
            rolePermission.setPermissionName(trimmedPermissionKey);

            rolePermissionMapper.insert(rolePermission);
        }

        return true;
    }

    private RoleVO toRoleVO(Role role) {
        RoleVO roleVO = new RoleVO();
        roleVO.setId(role.getId());
        roleVO.setRoleName(role.getRoleName());
        roleVO.setRoleKey(role.getRoleKey());
        roleVO.setStatus(role.getStatus());
        roleVO.setCreateTime(role.getCreateTime());

        if (role.getStatus() != null) {
            roleVO.setStatusText(role.getStatus() == 1 ? "启用" : "禁用");
        }

        return roleVO;
    }
}