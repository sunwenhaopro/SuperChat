package org.superchat.server.annotation;


import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Documented
@RestController
public @interface ResponseResult {
    boolean ignore() default false;
}
