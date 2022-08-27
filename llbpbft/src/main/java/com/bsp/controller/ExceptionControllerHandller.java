package com.bsp.controller;


import com.bsp.exceptions.CommonException;
import com.bsp.utils.LoggerHelper;
import com.bsp.web.Result;
import com.bsp.web.ResultStatus;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

/**
 * GlobalExceptionHandler
 *
 * @author 3
 */
@ControllerAdvice
public class ExceptionControllerHandller {

    /**
     * 处理自定义的业务异常
     *
     * @param e CommonException
     * @return Result
     */
    @ExceptionHandler(value = CommonException.class)
    @ResponseBody
    public Result<?> commonExceptionHandler(CommonException e) {
        LoggerHelper.error(e.getMessage(), e);
        if (Objects.isNull(e.getResultStatus())) {
            return Result.failure(e.getMessage());
        }
        return Result.failure(e.getResultStatus());
    }

    /**
     * 处理JSR303异常
     *
     * @param e Exception
     * @return Result
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public Result<?> jsrExceptionHandler(Exception e) {
        LoggerHelper.error(e.getMessage(), e);
        e.printStackTrace();
        return new Result<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
    }

    /**
     * 处理空指针异常
     *
     * @param e Exception
     * @return Result
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public Result<?> nullPointerExceptionHandler(Exception e) {
        LoggerHelper.error(e.getMessage(), e);
        e.printStackTrace();
        return new Result<>(ResultStatus.INTERNAL_SERVER_ERROR.getCode(), e.getMessage(), "空指针异常");
    }

    /**
     * 处理其他异常
     *
     * @param e Exception
     * @return Result
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result<?> exceptionHandler(Exception e) {
        LoggerHelper.error(e.getMessage(), e);
        e.printStackTrace();
        return new Result<>(ResultStatus.INTERNAL_SERVER_ERROR.getCode(), e.getMessage(), null);
    }
}
