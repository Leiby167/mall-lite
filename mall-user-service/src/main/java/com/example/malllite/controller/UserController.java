package com.example.malllite.controller;

import com.example.malllite.annotation.RequirePermission;
import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.LoginUserHolder;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.request.LoginRequest;
import com.example.malllite.request.UserAssignRoleRequest;
import com.example.malllite.request.UserRequest;
import com.example.malllite.service.UserService;
import com.example.malllite.vo.LoginUserVO;
import com.example.malllite.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequirePermission(permissionKey = "user:query")
    @GetMapping("/{id}")
    public BaseResponse<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return BaseResponse.success(userVO);
    }

    @PostMapping("/register")
    public BaseResponse<Boolean> register(@RequestBody @Valid UserRequest request) {
        return userService.register(request);
    }

    @RequirePermission(permissionKey = "user:add")
    @PostMapping("/add")
    public BaseResponse<Boolean> addUser(@RequestBody @Valid UserRequest request) {
        return userService.addUser(request);
    }

    @RequirePermission(permissionKey = "user:update")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody @Valid UserRequest request) {
        return userService.updateUser(request);
    }

    @RequirePermission(permissionKey = "user:delete")
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody @Valid LoginRequest request) {
        return BaseResponse.success(userService.login(request));
    }

    @GetMapping("/current")
    public BaseResponse<LoginUserVO> getCurrentUser() {
        LoginUserVO loginUserVO = LoginUserHolder.get();

        if (loginUserVO == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        return BaseResponse.success(loginUserVO);
    }

    @RequirePermission(permissionKey = "user:assign-role")
    @PostMapping("/assign-role")
    public BaseResponse<Boolean> assignRole(@RequestBody @Valid UserAssignRoleRequest request) {
        return userService.assignRole(request);
    }
}