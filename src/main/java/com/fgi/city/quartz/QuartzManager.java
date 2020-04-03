package com.fgi.city.quartz;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.stereotype.Repository;

@Repository("quartzManager")
public class QuartzManager {

    Logger log = LogManager.getLogger(QuartzManager.class);
    public static SchedulerFactory gSchedulerFactory;
    public static String JOB_GROUP_NAME;
    public static String TRIGGER_GROUP_NAME;

    static {
        gSchedulerFactory = new StdSchedulerFactory();
        JOB_GROUP_NAME = "EXTJWEB_JOBGROUP_NAME";
        TRIGGER_GROUP_NAME = "EXTJWEB_TRIGGERGROUP_NAME";
    }

    /**
     * @param jobName 任务名
     * @param cls     任务
     * @param time    时间设置，参考quartz说明文档
     * @Description: 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:47:44
     * @version V2.0
     */
    public void addJob(String jobName, Class cls, String time) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            JobDetailImpl jobDetail = new JobDetailImpl();// 任务名，任务组，任务执行类
            jobDetail.setName(jobName);
            jobDetail.setGroup(JOB_GROUP_NAME);
            jobDetail.setJobClass(cls);
            // 触发器
            CronTriggerImpl trigger = new CronTriggerImpl();// 触发器名,触发器组
            trigger.setName(jobName);
            trigger.setGroup(TRIGGER_GROUP_NAME);
            trigger.setCronExpression(time);// 触发器时间设定
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jobName          任务名
     * @param jobGroupName     任务组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass         任务
     * @param time             时间设置，参考quartz说明文档
     * @Description: 添加一个定时任务
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:48:15
     * @version V2.0
     */
    public void addJob(String jobName, String jobGroupName, String triggerName,
                       String triggerGroupName, Class jobClass, String time) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            JobDetailImpl jobDetail = new JobDetailImpl();// 任务名，任务组，任务执行类
            jobDetail.setName(jobName);
            jobDetail.setGroup(jobGroupName);
            jobDetail.setJobClass(jobClass);
            // 触发器
            CronTriggerImpl trigger = new CronTriggerImpl();// 触发器名,触发器组
            trigger.setName(triggerName);
            trigger.setGroup(triggerGroupName);
            trigger.setCronExpression(time);// 触发器时间设定
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jobName
     * @param time
     * @Description: 修改一个任务的触发时间(使用默认的任务组名 ， 触发器名 ， 触发器组名)
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:49:21
     * @version V2.0
     */
    public void modifyJobTime(String jobName, String time) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            CronTrigger trigger = (CronTrigger) sched.getTrigger(new TriggerKey(jobName,
                    TRIGGER_GROUP_NAME));
            if (trigger == null) {
                return;
            }
            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(time)) {
                JobDetail jobDetail = sched.getJobDetail(new JobKey(jobName,
                        JOB_GROUP_NAME));
                Class objJobClass = jobDetail.getJobClass();
                removeJob(jobName);
                addJob(jobName, objJobClass, time);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param triggerName
     * @param triggerGroupName
     * @param time
     * @Description: 修改一个任务的触发时间
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:49:37
     * @version V2.0
     */
    public boolean modifyJobTime(String triggerName, String triggerGroupName,
                                 String time) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();

            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);

            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(time);

            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
            }
            //按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder)
                    .build();

            //按新的trigger重新设置job执行
            sched.rescheduleJob(triggerKey, trigger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * @param jobName
     * @Description: 移除一个任务(使用默认的任务组名 ， 触发器名 ， 触发器组名)
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:49:51
     * @version V2.0
     */
    public void removeJob(String jobName) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            sched.pauseTrigger(new TriggerKey(jobName, TRIGGER_GROUP_NAME));// 停止触发器
            sched.unscheduleJob(new TriggerKey(jobName, TRIGGER_GROUP_NAME));// 移除触发器
            sched.deleteJob(new JobKey(jobName, JOB_GROUP_NAME));// 删除任务
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @Description: 移除一个任务
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:50:01
     * @version V2.0
     */
    public void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            sched.pauseTrigger(new TriggerKey(triggerName, triggerGroupName));// 停止触发器
            sched.unscheduleJob(new TriggerKey(triggerName, triggerGroupName));// 移除触发器
            sched.deleteJob(new JobKey(jobName, jobGroupName));// 删除任务
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description:启动所有定时任务
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:50:18
     * @version V2.0
     */
    public void startJobs() {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            sched.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description:关闭所有定时任务
     * @Title: QuartzManager.java
     * @Copyright: Copyright (c) 2014
     * @author Comsys-LZP
     * @date 2014-6-26 下午03:50:26
     * @version V2.0
     */
    public void shutdownJobs() {
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据jobname判断任务是否已经存在，true已经存在，false不存在
     *
     * @param jobName
     * @return boolean
     * @throws SchedulerException
     */
    public boolean isExist(String jobName) throws SchedulerException {
        boolean flag = false;
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            JobDetail jonDetail = sched.getJobDetail(new JobKey(jobName, JOB_GROUP_NAME));
            if (jonDetail != null) {
                flag = true;
            }
        } catch (SchedulerException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
        return flag;
    }

    /**
     * 根据jobname判断任务是否已经存在，true已经存在，false不存在
     *
     * @param jobName
     * @param jobName
     * @return boolean
     * @throws SchedulerException
     */
    public boolean isExist(String jobName, String groupName) throws SchedulerException {
        boolean flag = false;
        try {
            Scheduler sched = gSchedulerFactory.getScheduler();
            JobDetail jonDetail = sched.getJobDetail(new JobKey(jobName, groupName));
            if (jonDetail != null) {
                flag = true;
            }
        } catch (SchedulerException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
        return flag;
    }

}
