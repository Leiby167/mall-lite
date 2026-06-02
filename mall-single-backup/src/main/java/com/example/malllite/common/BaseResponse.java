package com.example.malllite.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    private int code;

    private T data;

    private String message;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ErrorCode.SUCCESS.getCode());
        response.setData(data);
        response.setMessage(ErrorCode.SUCCESS.getMessage());
        return response;
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(code);
        response.setData(null);
        response.setMessage(message);
        return response;
    }
}