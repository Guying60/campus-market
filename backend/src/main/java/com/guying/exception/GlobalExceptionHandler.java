package com.guying.exception;

import com.guying.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("数据输入错误:"+msg);
        return Result.error(msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("运行时异常:"+e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e) {
        log.warn("业务异常:"+e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public Result handleJwtException(JwtException e){
        log.warn("token异常:"+e.getMessage());
        return Result.error(e.getCode(),e.getMessage());
    }
}