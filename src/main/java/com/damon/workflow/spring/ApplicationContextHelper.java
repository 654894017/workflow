package com.damon.workflow.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component("workflowApplicationContextHelper")
public class ApplicationContextHelper implements ApplicationContextAware {
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    public static Class<?> getClassFromCache(String claz) {
        return classCache.computeIfAbsent(claz, key -> {
            try {
                return Class.forName(key);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found: " + key, e);
            }
        });
    }

    public static <T> T getBean(String claz) {
        // 动态加载类
        Class<?> clazz = getClassFromCache(claz);
        // 使用 getBean 方法传入 Class 类型，避免类型转换异常
        return (T) ApplicationContextHelper.applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return ApplicationContextHelper.applicationContext.getBean(name, requiredType);
    }

    public static <T> T getBean(Class<T> requiredType, Object... params) {
        return ApplicationContextHelper.applicationContext.getBean(requiredType, params);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }
}
