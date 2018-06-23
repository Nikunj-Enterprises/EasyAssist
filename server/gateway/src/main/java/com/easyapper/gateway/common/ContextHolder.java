package com.easyapper.gateway.common;

public class ContextHolder {
    private static ThreadLocal<AppContext> appContextThreadLocal = new ThreadLocal<>();

    public static void setAppContext(AppContext context){
        appContextThreadLocal.set(context);
    }

    public static AppContext getAppContext(){
        return appContextThreadLocal.get();
    }

    public static void clear(){
        appContextThreadLocal.remove();
    }
}
