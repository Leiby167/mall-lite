package com.example.malllite.interceptor;

import com.example.malllite.annotation.RequirePermission;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.LoginUserHolder;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public PermissionInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }

        if (requirePermission == null) {
            return true;
        }

        String permissionKey = requirePermission.permissionKey();
        if (!StringUtils.hasText(permissionKey)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "权限标识不能为空");
        }

        Long userId = LoginUserHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        boolean hasPermission = userService.checkUserPermission(userId, permissionKey);
        if (!hasPermission) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "权限不足，无法访问该接口");
        }

        return true;
    }
}