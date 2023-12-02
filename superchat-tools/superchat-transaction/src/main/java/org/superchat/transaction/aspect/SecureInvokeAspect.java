package org.superchat.transaction.aspect;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.superchat.transaction.annotation.SecureInvoke;
import org.superchat.transaction.domain.dto.SecureInvokeDTO;
import org.superchat.transaction.domain.entity.SecureInvokeRecord;
import org.superchat.transaction.service.SecureInvokeService;
import org.superchat.transaction.util.JsonUtils;
import org.superchat.transaction.util.SecureInvokerHolder;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Aspect
@AllArgsConstructor
public class SecureInvokeAspect {
    private final SecureInvokeService secureInvokeService;
    @Around("@annotation(secureInvoke)")
    public Object around(ProceedingJoinPoint joinPoint,SecureInvoke secureInvoke) throws Throwable {
        boolean async = secureInvoke.async();
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。
        if (SecureInvokerHolder.isTransaction() || !inTransaction) {
            return joinPoint.proceed();
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<String> parameters = Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
        //定位到具体方法
        SecureInvokeDTO dto = SecureInvokeDTO.builder()
                .args(JsonUtils.toStr(joinPoint.getArgs()))
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(JsonUtils.toStr(parameters))
                .build();
        SecureInvokeRecord record = SecureInvokeRecord.builder()
                .secureInvokeDTO(dto)
                .maxRetryTimes(secureInvoke.maxTryTimes())
                .nextRetryTime(DateUtil.offsetMinute(new Date(), (int) SecureInvokeService.RETRY_INTERVAL_MINUTES))
                .build();
        secureInvokeService.invoke(record, async);
        return null;
    }

}
