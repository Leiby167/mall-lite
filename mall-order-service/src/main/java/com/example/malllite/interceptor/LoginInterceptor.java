package com.example.malllite.interceptor;

import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.LoginUserHolder;
import com.example.malllite.common.TokenUtils;
import com.example.malllite.exception.BusinessException;
import com.example.malllite.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            token = request.getHeader("token");
        }

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        token = token.trim();

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length()).trim();
        }

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (!TokenUtils.validateToken(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录状态无效或已过期");
        }

        Long userId = TokenUtils.getUserId(token);
        String username = TokenUtils.getUsername(token);

        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录状态无效或已过期");
        }

        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(userId);
        loginUserVO.setUsername(username);
        loginUserVO.setToken(token);

        LoginUserHolder.set(loginUserVO);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        LoginUserHolder.remove();
    }
}