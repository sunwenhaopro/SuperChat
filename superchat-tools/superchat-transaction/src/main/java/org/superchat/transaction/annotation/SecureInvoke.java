package org.superchat.transaction.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface SecureInvoke {

    //默认异步
    boolean async()  default true;

    //最大重试次数
    int maxTryTimes() default 3;

}
