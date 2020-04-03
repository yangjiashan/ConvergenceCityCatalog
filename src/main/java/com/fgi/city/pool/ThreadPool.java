package com.fgi.city.pool;

import java.util.concurrent.ExecutorService;

/**
 * @author sirc_yjs
 * @Description 获取线程池的接口
 * @date 2019年3月6日
 */
public interface ThreadPool {

    /**
     * 获取线程池
     *
     * @return
     */
    ExecutorService getExecutor();

    /**
     * 关闭线程池，默认方法，先关闭队列，等待已在队列中的线程执行完成 方法在终止前允许执行以前提交的任务
     *
     * @param executor
     */
    default void shutdown(ExecutorService executor) {
        executor.shutdown();
    }

}