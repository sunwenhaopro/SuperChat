package org.superchat.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//事务默认是同步阻塞的，配置线程池后异步
@Configuration
public class SpringEventConfig {
        /**
         * 向 Spring 容器中注册一个事件广播器，事件广播器的接口是 ApplicationEventMulticaster，这个事件广播器只有一个实现类 SimpleApplicationEventMulticaster
         * 所以只能向 Spring 中注册 SimpleApplicationEventMulticaster
         * <p>
         * ApplicationEventMulticaster 就是用来广播事件的，默认情况下，Spring 容器在启动的时候，会去自动查找一个名为 applicationEventMulticaster 的事件广播器，
         * 如果找到了，就直接使用这个事件广播器，如果没有找到，则自动创建一个 SimpleApplicationEventMulticaster 类型的事件广播器。
         * 所以，如果我们想要自定义事件广播器，则自定义的事件广播器的名字必须是 applicationEventMulticaster。
         *
         * @return
         */
        @Bean
        ApplicationEventMulticaster applicationEventMulticaster() {
            SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
            ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
            taskExecutor.setThreadNamePrefix("superchat:event:");
            taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
            taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 5);
            taskExecutor.setQueueCapacity(Runtime.getRuntime().availableProcessors() * 2);
            taskExecutor.initialize();
            taskExecutor.setThreadFactory(new MyThreadFactory(taskExecutor));// 异常捕获
            multicaster.setTaskExecutor(taskExecutor);
            return multicaster;
        }
}
