package com.fgi.city.pool;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author sirc_yjs
 * @Description 线程池管理类
 * @date 2019年3月6日
 */
public class ThreadPoolManage {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolManage.class);
    private static boolean isShutdown = false;
    private static ThreadPoolManage instance = null;

    private ThreadPoolManage() {
    }

    private static synchronized void init() {
        if (instance == null) {
            instance = new ThreadPoolManage();
        }
    }

    public static ThreadPoolManage getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    /**
     * 线程池集合
     */
    private static final ConcurrentHashMap<String, ExecutorService> executorMap = new ConcurrentHashMap<String, ExecutorService>();

    /**
     * 传入class获取对应的线程池
     *
     * @param clazz
     * @return
     */
    public static ExecutorService getExecutor(Class<? extends ThreadPool> clazz) {
        // 判断线程池是否已创建过
        if (!executorMap.containsKey(clazz.getName()) && !isShutdown) {
            putExecutor(clazz);
        }
        return executorMap.get(clazz.getName());
    }

    /**
     * 创建线程池，并存入executorMap中
     *
     * @param clazz
     */
    private static synchronized void putExecutor(Class<? extends ThreadPool> clazz) {
        if (!executorMap.containsKey(clazz.getName()) && !isShutdown) {
            ExecutorService executor = null;
            try {
                // 创建线程池
                executor = ((ThreadPool) Class.forName(clazz.getName()).newInstance()).getExecutor();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            executorMap.put(clazz.getName(), executor);

        }
    }

    /**
     * 执行一个线程
     *
     * @param clazz
     * @param command
     */
    public static void execute(Class<? extends ThreadPool> clazz, Runnable command) {
        ExecutorService executor = getExecutor(clazz);
        executor.execute(command);
    }

    /**
     * 提交一个线程
     *
     * @param clazz
     * @param task
     * @return
     */
    public static <T> Future<T> submit(Class<? extends ThreadPool> clazz, Callable<T> task) {
        ExecutorService executor = getExecutor(clazz);
        return executor.submit(task);
    }

    /**
     * 关闭所有线程池
     */
    public static void shutdownAll() {
        isShutdown = true;
        ThreadPool threadPool = null;
        for (Entry<String, ExecutorService> item : executorMap.entrySet()) {
            try {
                threadPool = (ThreadPool) Class.forName(item.getKey()).newInstance();
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
            // 关闭线程池,停止接收线程
            if (threadPool != null) { // 担心创建ThreadPool实例时出现异常，threadPool对象为null,再调shutdown方法会报空指针
                threadPool.shutdown(item.getValue());
            } else {
                item.getValue().shutdown();
            }
            log.info(item.getKey() + "线程池正在关闭...");
        }

        // 存储已关闭线程池的key
        List<String> keyList = new ArrayList<String>(executorMap.size());

        while (true) {
            for (Entry<String, ExecutorService> item : executorMap.entrySet()) {
                // 判断线程是否都执行完了
                if (item.getValue().isTerminated()) {
                    keyList.add(item.getKey());
                    log.info(item.getKey() + "线程池已关闭...");
                }
            }

            // 根据key移除已关闭的线程池
            for (String key : keyList) {
                executorMap.remove(key);
            }

            if (executorMap.size() == 0) {
                break;
            }

            // 清空集合
            keyList.clear();
            try {
                // 睡眠2秒
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        }
        log.info("线程池已全部关闭...");
    }

    /**
     * 关闭单个线程池
     *
     * @param clazz
     */
    public static void shutdownExecutor(Class<? extends ThreadPool> clazz) {
        shutdownExecutor(clazz.getName());
    }

    /**
     * 关闭单个线程池
     *
     * @param name
     */
    private static void shutdownExecutor(String name) {
        ExecutorService executor = executorMap.get(name);
        ThreadPool threadPool = null;
        if (executor != null) {

            try {
                threadPool = (ThreadPool) Class.forName(name).newInstance();
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
            // 关闭线程池,停止接收线程
            if (threadPool != null) { // 担心创建ThreadPool实例时出现异常，threadPool对象为null,再调shutdown方法会报空指针
                threadPool.shutdown(executor);
            } else {
                executor.shutdown();
            }
        }
        log.info(name + "线程池已关闭...");
        executorMap.remove(name);
    }

}
