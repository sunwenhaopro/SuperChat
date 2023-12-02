package org.superchat.transaction.util;

import java.util.Objects;

public class SecureInvokerHolder {
    private static final ThreadLocal<Boolean> threadLocal=new ThreadLocal<>();

    public static void remove()
    {
        threadLocal.remove();
    }
    public static void setTransaction()
    {
        threadLocal.set(Boolean.TRUE);
    }
    public static boolean isTransaction()
    {
       return Objects.nonNull(threadLocal.get());
    }
}
