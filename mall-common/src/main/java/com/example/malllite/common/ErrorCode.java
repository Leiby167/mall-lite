package com.example.malllite.common;

public enum ErrorCode {
    SUCCESS(0, "ok"),

    PARAMS_ERROR(40000, "参数错误"),

    NOT_LOGIN_ERROR(40100, "用户未登录"),

    PERMISSION_DENIED(40300, "权限不足"),

    NOT_FOUND_ERROR(40400, "数据不存在"),

    SYSTEM_ERROR(50000, "系统内部异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}