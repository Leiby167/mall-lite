package com.example.malllite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.TokenUtils;
import com.example.malllite.entity.Role;
import com.example.malllite.entity.RolePermission;
import com.example.malllite.entity.User;
import com.example.malllite.entity.UserRole;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.mapper.RoleMapper;
import com.example.malllite.mapper.RolePermissionMapper;
import com.example.malllite.mapper.UserMapper;
import com.example.malllite.mapper.UserRoleMapper;
import com.example.malllite.request.LoginRequest;
import com.example.malllite.request.UserAssignRoleRequest;
import com.example.malllite.request.UserRequest;
import com.example.malllite.service.UserService;
import com.example.malllite.vo.LoginUserVO;
import com.example.malllite.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private static final String DEFAULT_ROLE_KEY = "user";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public UserVO getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setStatus(user.getStatus());

        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, id);
        UserRole userRole = userRoleMapper.selectOne(queryWrapper);

        if (userRole != null) {
            Role role = roleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                userVO.setRoleId(role.getId());
                userVO.setRoleName(role.getRoleName());
                userVO.setRoleKey(role.getRoleKey());
            }
        }

        return userVO;
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> register(UserRequest request) {
        createUser(request, 1, true);
        return BaseResponse.success(true);
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> addUser(UserRequest request) {
        Integer status = request == null || request.getStatus() == null ? 1 : request.getStatus();
        createUser(request, status, true);
        return BaseResponse.success(true);
    }

    private void createUser(UserRequest request, Integer status, boolean bindDefaultRole) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能为空");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }

        validateStatus(status);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername().trim());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(status);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加用户失败");
        }

        if (bindDefaultRole) {
            bindDefaultUserRole(user.getId());
        }
    }

    private void bindDefaultUserRole(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }

        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleKey, DEFAULT_ROLE_KEY)
                .last("limit 1");

        Role defaultRole = roleMapper.selectOne(roleQueryWrapper);
        if (defaultRole == null) {
            return;
        }

        LambdaQueryWrapper<UserRole> userRoleQueryWrapper = new LambdaQueryWrapper<>();
        userRoleQueryWrapper.eq(UserRole::getUserId, userId);
        UserRole existUserRole = userRoleMapper.selectOne(userRoleQueryWrapper);
        if (existUserRole != null) {
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(defaultRole.getId());

        int rows = userRoleMapper.insert(userRole);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "绑定默认角色失败");
        }
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> updateUser(UserRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }

        User oldUser = userMapper.selectById(request.getId());
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        if (StringUtils.hasText(request.getUsername())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, request.getUsername().trim())
                    .ne(User::getId, request.getId());

            User sameUsernameUser = userMapper.selectOne(queryWrapper);
            if (sameUsernameUser != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
            }
        }

        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
        }

        User user = new User();
        user.setId(request.getId());

        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername().trim());
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        }

        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户失败");
        }

        return BaseResponse.success(true);
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        User oldUser = userMapper.selectById(id);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        if (oldUser.getStatus() != null && oldUser.getStatus() == 0) {
            return BaseResponse.success(true);
        }

        User user = new User();
        user.setId(id);
        user.setStatus(0);

        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "禁用用户失败");
        }

        return BaseResponse.success(true);
    }

    @Override
    public LoginUserVO login(LoginRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername().trim());

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        if (!passwordMatches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "用户被禁用");
        }

        if (needUpgradePassword(user.getPassword())) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
            userMapper.updateById(updateUser);
        }

        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUsername(user.getUsername());

        String token = TokenUtils.generateToken(user.getId(), user.getUsername());
        loginUserVO.setToken(token);

        return loginUserVO;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }

        if (isBCryptPassword(storedPassword)) {
            return PASSWORD_ENCODER.matches(rawPassword, storedPassword);
        }

        return storedPassword.equals(rawPassword);
    }

    private boolean needUpgradePassword(String storedPassword) {
        return StringUtils.hasText(storedPassword) && !isBCryptPassword(storedPassword);
    }

    private boolean isBCryptPassword(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> assignRole(UserAssignRoleRequest request) {
        if (request == null || request.getUserId() == null || request.getRoleId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID或角色ID不能为空");
        }

        if (request.getUserId() <= 0 || request.getRoleId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID或角色ID不合法");
        }

        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }

        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, request.getUserId());
        userRoleMapper.delete(queryWrapper);

        UserRole userRole = new UserRole();
        userRole.setUserId(request.getUserId());
        userRole.setRoleId(request.getRoleId());

        int rows = userRoleMapper.insert(userRole);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "绑定角色失败");
        }

        return BaseResponse.success(true);
    }

    @Override
    public boolean checkUserPermission(Long userId, String permissionKey) {
        if (userId == null || userId <= 0 || !StringUtils.hasText(permissionKey)) {
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            return false;
        }

        LambdaQueryWrapper<UserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(UserRole::getUserId, userId);
        UserRole userRole = userRoleMapper.selectOne(userRoleQuery);

        if (userRole == null) {
            return false;
        }

        Long roleId = userRole.getRoleId();

        QueryWrapper<RolePermission> rolePermissionQuery = new QueryWrapper<>();
        rolePermissionQuery.eq("role_id", roleId)
                .eq("permission_key", permissionKey);

        RolePermission rolePermission = rolePermissionMapper.selectOne(rolePermissionQuery);

        return rolePermission != null;
    }

    private void validateStatus(Integer status) {
        if (status == null) {
            return;
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户状态不合法");
        }
    }
}