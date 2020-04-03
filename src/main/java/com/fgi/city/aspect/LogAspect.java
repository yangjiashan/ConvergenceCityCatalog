package com.fgi.city.aspect;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.pool.ThreadPoolImpl;
import com.fgi.city.pool.ThreadPoolManage;
import com.fgi.city.task.LogTask;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutorService;

@Aspect
@Component("logAspect")
public class LogAspect {

    private ExecutorService executorService = ThreadPoolManage.getExecutor(ThreadPoolImpl.class);

    // 配置织入点
    @Pointcut("@annotation(com.fgi.city.aspect.Log)")
    public void logPointCut() {

    }

    /**
     * AOP环绕通知
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object doArround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object res = joinPoint.proceed(); // 方法执行
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String[] strings = request.getRequestURI().split("/");
        String interfacePath = strings[strings.length - 1];
        executorService.execute(new LogTask((JSONObject) args[0], (JSONObject) args[1], interfacePath));
        return res;
    }
}
