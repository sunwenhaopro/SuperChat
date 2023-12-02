package org.superchat.transaction.annotation;

import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;

public interface SecureInvokerConfigurer {
    @Nullable
    default Executor getSecureInvokeExecutor(){
          return null;
    }
}
