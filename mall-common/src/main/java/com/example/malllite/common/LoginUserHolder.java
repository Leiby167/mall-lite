package com.example.malllite.common;

import com.example.malllite.vo.LoginUserVO;

public class LoginUserHolder {

    private static final ThreadLocal<LoginUserVO> USER_THREAD_LOCAL = new ThreadLocal<>();

    private LoginUserHolder() {
    }

    public static void set(LoginUserVO loginUserVO) {
        USER_THREAD_LOCAL.set(loginUserVO);
    }

    public static LoginUserVO get() {
        return USER_THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        LoginUserVO loginUserVO = get();
        return loginUserVO == null ? null : loginUserVO.getId();
    }

    public static String getUsername() {
        LoginUserVO loginUserVO = get();
        return loginUserVO == null ? null : loginUserVO.getUsername();
    }

    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}