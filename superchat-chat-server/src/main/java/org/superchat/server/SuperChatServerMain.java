package org.superchat.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@EnableCaching
@ServletComponentScan
@MapperScan(basePackages = "org.superchat.server.**.mapper")
@SpringBootApplication(scanBasePackages = {"org.superchat"})
public class SuperChatServerMain {
    public static void main(String[] args) {
        SpringApplication.run(SuperChatServerMain.class);
    }
}
