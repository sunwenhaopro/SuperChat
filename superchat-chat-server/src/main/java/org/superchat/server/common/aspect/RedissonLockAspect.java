package org.superchat.server.common.aspect;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.superchat.server.annotation.RedissonLock;
import org.superchat.server.common.service.LockService;
import org.superchat.server.common.utils.SpELUtil;

import java.lang.reflect.Method;


@Order(0) //优先于事务执行
@Aspect
@Slf4j
@Component
@AllArgsConstructor
public class RedissonLockAspect {

    private final LockService lockService;

    @Around(value = "@annotation(org.superchat.server.annotation.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint)  throws Throwable{
        //获取方法
        Method method=  ((MethodSignature) joinPoint.getSignature()).getMethod();
        //获取注解
        RedissonLock redissonLock=method.getAnnotation(RedissonLock.class);
        //默认方法限定名+注解排名（可能多个）
        String prefix = StrUtil.isBlank(redissonLock.prefixKey()) ? SpELUtil.getMethodKey(method) : redissonLock.prefixKey();
        String key = SpELUtil.parseSpEL(method, joinPoint.getArgs(), redissonLock.key());
        return lockService.executeLockWithThrows(prefix + ":" + key, redissonLock.waitTime(), redissonLock.timeUnit(), joinPoint::proceed);
    }
}
