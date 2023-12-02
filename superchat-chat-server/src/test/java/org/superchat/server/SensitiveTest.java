package org.superchat.server;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.superchat.server.annotation.RedissonLock;

import javax.annotation.Resource;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SensitiveTest {

    @Resource
    ThreadPoolTaskExecutor executor;
    @Test
    public void  test1()
    {
        executor.execute(()->{
            throw new RuntimeException("345");
        });
    }

    @Test
    @RedissonLock(key = "8888")
    public void test2()
    {
        System.out.println("hello world");
    }
}
