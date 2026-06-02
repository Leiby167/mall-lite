package com.example.malllite.service;

import com.example.malllite.common.BaseResponse;
import com.example.malllite.request.LoginRequest;
import com.example.malllite.request.UserAssignRoleRequest;
import com.example.malllite.request.UserRequest;
import com.example.malllite.vo.LoginUserVO;
import com.example.malllite.vo.UserVO;

public interface UserService {

    UserVO getUserById(Long id);

    BaseResponse<Boolean> register(UserRequest request);

    BaseResponse<Boolean> addUser(UserRequest request);

    BaseResponse<Boolean> updateUser(UserRequest request);

    BaseResponse<Boolean> deleteUser(Long id);

    LoginUserVO login(LoginRequest request);

    BaseResponse<Boolean> assignRole(UserAssignRoleRequest request);

    boolean checkUserPermission(Long userId, String permissionKey);
}