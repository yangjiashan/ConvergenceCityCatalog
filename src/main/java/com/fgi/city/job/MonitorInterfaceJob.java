package com.fgi.city.job;

import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.pool.ThreadPoolImpForMonitor;
import com.fgi.city.pool.ThreadPoolManage;
import com.fgi.city.task.MonitorInterfaceTask;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author sirc_yjs
 * @Description 探测地市上报的接口
 * @date 2019年3月6日
 */
public class MonitorInterfaceJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(MonitorInterfaceJob.class);
    private ExecutorService es = ThreadPoolManage.getExecutor(ThreadPoolImpForMonitor.class);
    private InterfaceMapper interfaceMapper = BeanProvider.getBean(InterfaceMapper.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            // 查询未检测过的接口方法（要有参数才检测，避免先传方法后还没来得及传参数时被检查认定为失败）
            List<Map<String, Object>> interfaceList = interfaceMapper.queryInterfaceMethodCondition("0");
            if (interfaceList != null) {
                for (Map<String, Object> map : interfaceList) {
                    String methodId = String.valueOf(map.get("ID"));
                    int fieldCounts = Integer.parseInt(String.valueOf(map.get("FIELDCOUNT")));
                    if (fieldCounts <= 0) {
                        // 还没有参数暂时不作检测
                        logger.info("方法ID:"+methodId + "，暂时缺少参数 不作检测！");
                        continue;
                    }
                    es.execute(new MonitorInterfaceTask(methodId));
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
