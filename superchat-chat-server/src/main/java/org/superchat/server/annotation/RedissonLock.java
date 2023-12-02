package org.superchat.server.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RedissonLock {
  String prefixKey() default "";
  String key();
  int waitTime() default -1;
  TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
