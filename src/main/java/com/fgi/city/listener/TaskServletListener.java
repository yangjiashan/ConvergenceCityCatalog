package com.fgi.city.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.fgi.city.job.MonitorInterfaceJob;
import com.fgi.city.pool.ThreadPoolManage;
import com.fgi.city.quartz.QuartzManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;


@WebListener
public class TaskServletListener implements ServletContextListener {
    private final Logger LOGGER = LoggerFactory.getLogger(TaskServletListener.class);
    private QuartzManager quartManager = new QuartzManager();
    private String monitorInterfaceTaskName = "monitorInterfaceTaskName";

    @Value("${monitorInterfaceTime.cron.expression}")
    private String monitorTime;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("++++++++++++++++++++++++容器初始化++++++++++++++++++++++++");
        LOGGER.info("++++++++++++++++++++++++容器初始化++++++++++++++++++++++++");
        LOGGER.info("++++++++++++++++++++++++容器初始化++++++++++++++++++++++++");
        // 开启任务
        startMonitorInterfaceJob(monitorTime);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("++++++++++++++++++++++++程序已经关闭++++++++++++++++++++++++");
        LOGGER.info("++++++++++++++++++++++++程序已经关闭++++++++++++++++++++++++");
        LOGGER.info("++++++++++++++++++++++++程序已经关闭++++++++++++++++++++++++");
        quartManager.shutdownJobs();
        ThreadPoolManage.getInstance().shutdownAll();
    }

    /**
     * 开启定时探测地市接口
     *
     * @param time
     */
    public void startMonitorInterfaceJob(String time) {
        LOGGER.info("开启定时探测地市接口可通性....");
        quartManager.addJob(monitorInterfaceTaskName, MonitorInterfaceJob.class, time);
    }
}
