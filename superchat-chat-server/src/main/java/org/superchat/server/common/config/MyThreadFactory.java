package org.superchat.server.common.config;


import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.superchat.server.common.handler.MyUncaughtExceptionHandler;

import java.util.concurrent.ThreadFactory;
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {
    private static final MyUncaughtExceptionHandler myUncaughtExceptionHandler=new MyUncaughtExceptionHandler();
    private final ThreadFactory threadFactory;
    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread=this.threadFactory.newThread(r);
        thread.setUncaughtExceptionHandler(myUncaughtExceptionHandler);
        return thread;
    }
}
