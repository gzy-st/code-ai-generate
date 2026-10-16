package com.guozy.codeaigenerate.ai.monitor;

/**
 * 监控上下文持有者（同线程内共享）
 */
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}