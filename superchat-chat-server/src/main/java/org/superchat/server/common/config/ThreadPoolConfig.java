package org.superchat.server.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.superchat.server.common.handler.MyUncaughtExceptionHandler;
import org.superchat.transaction.annotation.SecureInvokerConfigurer;
import org.superchat.transaction.service.SecureInvokeService;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class ThreadPoolConfig implements AsyncConfigurer , SecureInvokerConfigurer {
    private static final int CORE_POOL_SIZE = 10;
    public static final String BEAN_NAME = "threadPoolTaskExecutor";
    public static final String WS_EXECUTOR="webSocketTaskExecutor";
    public static final String SECURE_EXECUTOR="secureInvokeTaskExecutor";

    private static final  ThreadFactory threadFactory=(Runnable r) -> {
        Thread t = new Thread(r);
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        return t;
    };
    @Override
    public Executor getAsyncExecutor() {
        return this.threadPoolTaskExecutor();
    }


    @Override
    public  Executor getSecureInvokeExecutor()
    {
        return this.threadPoolTaskExecutor();
    }

    @Bean(BEAN_NAME)
    @Primary
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(CORE_POOL_SIZE*2);
        //优雅停机
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("superchat-executor-");
        //满了之后请求线程直接运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //线程异常捕捉
        executor.setThreadFactory(new MyThreadFactory(executor));
//        executor.setThreadFactory(threadFactory);
        executor.initialize();
        return executor;
    }
    @Bean(WS_EXECUTOR)
    public ThreadPoolTaskExecutor wsThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        //优雅停机
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("superchat-wsExecutor-");
        //满了之后请求线程直接运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        //线程异常捕捉
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }
}
