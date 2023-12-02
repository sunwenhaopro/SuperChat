package org.superchat.server.common.utils;

import java.util.Map;
import java.util.Objects;

public class ThreadLocalUtil {
    private static ThreadLocal<Map<String,Object>> threadLocal = new ThreadLocal<>();
    public static final String UID="uid";
    public static Long getUid()
    {
        return getLongValue(UID);
    }
    public  static Object getValue(String name)
    {
        return threadLocal.get().get(name);
    }
    public static Long getLongValue(String key)
    {
        return Long.parseLong(threadLocal.get().get(key).toString());
    }
    public static String  getString(String name)
    {
        return Objects.isNull(threadLocal.get().get(name)) ? "" : threadLocal.get().get(name).toString();
    }
    public static void setMap(Map<String,Object> map)
    {
        threadLocal.set(map);
    }
    public static void remove()
    {
        threadLocal.remove();
    }

}
