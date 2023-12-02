package org.superchat.server.common.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.superchat.server.common.exception.Enum.CommonErrorEnum;
import org.superchat.server.common.utils.AssertUtil;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@AllArgsConstructor
@Service
public class LockService {
    private final RedissonClient redissonClient;

    @SneakyThrows
    public <T> T executeLockWithThrows(String key, int waitTime, TimeUnit timeUnit, SupplierThrow<T> supplier) throws Throwable {
        RLock lock= redissonClient.getLock(key);
        boolean lockResult=lock.tryLock(waitTime,timeUnit);
        AssertUtil.isTrue(lockResult, CommonErrorEnum.LOCK_LIMIT);
        try{
            return supplier.get();
        }finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    @SneakyThrows
    public <T> T executeWithLock(String key, Supplier<T> supplier) throws Throwable {
       return  executeLockWithThrows(key,-1,TimeUnit.MILLISECONDS,supplier::get);
    }

    @FunctionalInterface
    public interface SupplierThrow<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }
}
