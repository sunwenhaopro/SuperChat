package org.superchat.server.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.superchat.server.common.domain.vo.response.ApiResult;
import org.superchat.server.common.exception.BusinessException;
import org.superchat.server.common.exception.Enum.CommonErrorEnum;

@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResult<?> methodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        StringBuilder errorMsg=new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(o->errorMsg.append(o.getField()).append(o.getDefaultMessage()).append(","));
        String message=errorMsg.toString();
        log.info("\n validation parameters error！The reason is:{} \n", message);
        return ApiResult.fail(CommonErrorEnum.PARAM_VALID.getCode(),message.substring(0,message.length()-1));
    }

    @ExceptionHandler(value = BusinessException.class)
    public ApiResult<?> throwable(BusinessException e)
    {
        log.error("Business Error! Caused By:",e.getErrorCode());
        return ApiResult.fail(e.getErrorCode(),e.getErrorMsg());
    }

    @ExceptionHandler(value = Throwable.class)
    public ApiResult<?> throwable(Throwable e)
    {
        log.error("System Error! Caused By:",e);
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
    }
}
