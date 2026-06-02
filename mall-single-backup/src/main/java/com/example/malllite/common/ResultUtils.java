package com.example.malllite.common;

public class ResultUtils {

    private ResultUtils() {
    }

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.success(data);
    }

    public static BaseResponse<Boolean> success() {
        return BaseResponse.success(true);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return BaseResponse.error(code, message);
    }
}