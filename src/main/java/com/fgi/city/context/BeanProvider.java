package com.fgi.city.context;

import org.springframework.context.ApplicationContext;

public abstract class BeanProvider {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        BeanProvider.applicationContext = applicationContext;
    }

    protected BeanProvider() {
    }

    /**
     * CustomApplicationContextAware 处注入
     *
     * @param applicationContext
     */
    public static void initialize(ApplicationContext applicationContext) {
        BeanProvider.applicationContext = applicationContext;
    }

    /**
     * Get Bean by clazz.
     *
     * @param clazz Class
     * @param <T>   class type
     * @return Bean instance
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String beanId) {
        if (applicationContext == null) {
            return null;
        }
        return (T) applicationContext.getBean(beanId);
    }

}