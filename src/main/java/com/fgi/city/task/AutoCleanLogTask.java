package com.fgi.city.task;

import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.LogMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author sirc_yjs
 * @Description 自动删除日志
 * @date 2019年2月22日
 */
@Component
public class AutoCleanLogTask implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(AutoCleanLogTask.class);

    @Autowired
    private LogMapper logMapper;

    @Autowired
    private ConfigBean configBean;


    public AutoCleanLogTask() {
    }

    @Scheduled(cron = "${autoCleanLogCronExpression}")
    public void execute() {
        String enabled = configBean.getAutoCleanlogEnable();
        if ("0".equals(enabled)) {
            return;
        }
        try {
            logMapper.cleanLogByDay(Integer.parseInt(configBean.getLogKeepDay()));
            logger.info("自动删除日志...");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(logMapper, "logService is null");
    }

}
