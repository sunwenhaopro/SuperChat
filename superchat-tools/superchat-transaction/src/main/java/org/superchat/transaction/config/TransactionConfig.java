package org.superchat.transaction.config;

import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;
import org.superchat.transaction.annotation.SecureInvokerConfigurer;
import org.superchat.transaction.aspect.SecureInvokeAspect;
import org.superchat.transaction.dao.SecureInvokeRecordDao;
import org.superchat.transaction.mapper.SecureInvokeRecordMapper;
import org.superchat.transaction.service.SecureInvokeService;
import org.superchat.transaction.util.MqProducer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Configuration
@EnableScheduling
@ComponentScan("org.superchat.transaction")
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
@Import({SecureInvokeAspect.class, SecureInvokeRecordDao.class})
public class TransactionConfig {

    @Nullable
    protected  Executor executor;

    @Autowired
    void setConfigurers(ObjectProvider<SecureInvokerConfigurer> configurers) {
        Supplier<SecureInvokerConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokerConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one AsyncConfigurer may exist");
            }
            return candidates.get(0);
        });
        this.executor = Optional.ofNullable(configurer.get()).map(SecureInvokerConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }
    @Bean
    public MqProducer getMQProducer()
    {
        return new MqProducer();
    }
    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDao dao) {
        return new SecureInvokeService(dao, executor);
    }
}
