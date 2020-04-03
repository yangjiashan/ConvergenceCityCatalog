package com.fgi.city.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sirc_yjs
 * @Description 创建添加日志使用的线程池，线程最大数20，核心线程数20；
 * @date 2019年2月15日
 */
public class ThreadPoolImpl implements ThreadPool {

    @Override
    public ExecutorService getExecutor() {
        return new ThreadPoolExecutor(20, 20,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

}
