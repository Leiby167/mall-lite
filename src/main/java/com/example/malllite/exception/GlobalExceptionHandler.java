package com.example.malllite.exception;

import com.example.malllite.common.BaseResponse;
import com.example.malllite.common.ErrorCode;
import com.example.malllite.common.ResultUtils;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : ErrorCode.PARAMS_ERROR.getMessage();

        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<?> constraintViolationExceptionHandler(ConstraintViolationException e) {
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("系统异常", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }
}