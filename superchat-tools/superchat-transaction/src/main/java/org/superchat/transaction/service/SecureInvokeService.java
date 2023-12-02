package org.superchat.transaction.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.superchat.transaction.dao.SecureInvokeRecordDao;
import org.superchat.transaction.domain.dto.SecureInvokeDTO;
import org.superchat.transaction.domain.entity.SecureInvokeRecord;
import org.superchat.transaction.util.JsonUtils;
import org.superchat.transaction.util.SecureInvokerHolder;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Lazy
@Component
public class SecureInvokeService {
    public final static Integer RETRY_INTERVAL_MINUTES = 2;

    private final SecureInvokeRecordDao secureInvokeRecordDao;

    private  final Executor executor;

    public void save(SecureInvokeRecord secureInvokeRecord) {
        secureInvokeRecordDao.save(secureInvokeRecord);
    }

    public void retryRecord(SecureInvokeRecord record, String errorMsg) {
        Integer times = record.getRetryTimes() + 1;
        SecureInvokeRecord retryRecord = new SecureInvokeRecord();
        retryRecord.setRetryTimes(times);
        retryRecord.setFailReason(errorMsg);
        retryRecord.setUpdateTime(getNextRetryTime(times));
        if (times > record.getMaxRetryTimes()) {
            retryRecord.setStatus(SecureInvokeRecord.STATUS_FAIL);
        } else {
            retryRecord.setStatus(SecureInvokeRecord.STATUS_WAIT);
        }
        secureInvokeRecordDao.updateById(retryRecord);
    }
//    @Async
//    @Scheduled(cron = "*/5 * * * * ?")
//    public void retry() {
//        List<SecureInvokeRecord> secureInvokeRecords = secureInvokeRecordDao.getWaitRetryRecords();
//        for (SecureInvokeRecord secureInvokeRecord : secureInvokeRecords) {
//            doInvokeAsync(secureInvokeRecord);
//        }
//    }

    private Date getNextRetryTime(Integer times) {
        double range = Math.pow(RETRY_INTERVAL_MINUTES, times);
        Random random = new Random();
        int next = random.nextInt((int) range);
        return DateUtil.offsetMinute(new Date(), next);
    }

    public void removeRecord(SecureInvokeRecord record) {
        secureInvokeRecordDao.removeById(record.getId());
    }

    public void invoke(SecureInvokeRecord record, boolean async) {
        boolean isTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        if (!isTransaction) {
            return;
        }
        save(record);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @SneakyThrows
            @Override
            public void afterCommit() {
                if (async) {
                    doInvokeAsync(record);
                } else {
                    doInvoke(record);
                }
            }
        });
    }


    private void doInvokeAsync(SecureInvokeRecord record) {
        executor.execute(() -> {
            doInvoke(record);
        });
    }

    private void doInvoke(SecureInvokeRecord record) {
        SecureInvokeDTO secureInvokeDTO = record.getSecureInvokeDTO();
        try {
            SecureInvokerHolder.setTransaction();
            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass);
            List<String> parameterStrings = JsonUtils.toList(secureInvokeDTO.getParameterTypes(), String.class);
            List<Class<?>> parameterClasses = getParameters(parameterStrings);
            Method method = ReflectUtil.getMethod(beanClass,secureInvokeDTO.getMethodName(),parameterClasses.toArray(new Class[]{}));
            Object[] args=getArgs(secureInvokeDTO,parameterClasses);
            method.invoke(parameterStrings);
            removeRecord(record);
        } catch (Exception e) {
            retryRecord(record, e.getMessage());
        } finally {
            SecureInvokerHolder.remove();
        }
    }

    @NotNull
    private  List<Class<?>> getParameters(List<String> parameterStrings) {
        return parameterStrings.stream().map(parameterString -> {
            try {
                return Class.forName(parameterString);
            } catch (ClassNotFoundException e) {
                log.error("SecureInvokeService class not fund", e);
            }
            return null;
        }).collect(Collectors.toList());
    }

    @NotNull
    private Object[] getArgs(SecureInvokeDTO secureInvokeDTO,  List<Class<?>> parameterClasses) {
        JsonNode jsonNode=JsonUtils.toJsonNode(secureInvokeDTO.getArgs());
        Object[] args=new Object[jsonNode.size()];
        for (int i = 0; i < jsonNode.size(); i++) {
            Class<?> aClass= parameterClasses.get(i);
            args[i]=JsonUtils.nodeToValue(jsonNode.get(i),aClass);
        }
        return args;
    }

}
