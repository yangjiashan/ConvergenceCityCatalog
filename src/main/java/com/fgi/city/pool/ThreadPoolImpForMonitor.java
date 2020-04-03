package com.fgi.city.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 定时探测接口
 */
public class ThreadPoolImpForMonitor implements ThreadPool{
    @Override
    public ExecutorService getExecutor() {
        return new ThreadPoolExecutor(20, 30,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }
}
