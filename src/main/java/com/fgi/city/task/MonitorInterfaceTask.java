package com.fgi.city.task;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.client.ClientHandler;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.enums.InterfacePublishStateEnum;
import com.fgi.city.template.Generate_Template_Http;
import com.fgi.city.template.Generate_Template_Soap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 定时检测接口
 */
public class MonitorInterfaceTask implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(MonitorInterfaceTask.class);
    private InterfaceMapper interfaceMapper = BeanProvider.getBean(InterfaceMapper.class);
    private Generate_Template_Http autoCreate = BeanProvider.getBean(Generate_Template_Http.class);
    private Generate_Template_Soap soapCreate = BeanProvider.getBean(Generate_Template_Soap.class);

    private String methodId;

    public MonitorInterfaceTask(String methodId) {
        this.methodId = methodId;
    }

    @Override
    public void run() {
        JSONObject resultJson = null;
        try {
            // 检测接口是否可以封装（可通）
            ClientHandler clientHandler = new ClientHandler(methodId);
            String resultStr = clientHandler.execute();
            resultJson = JSONObject.parseObject(resultStr);
            resultJson.put("id", methodId);
            // 没有异常情况下，保存探测结果
            interfaceMapper.updateInterfaceMonitorStatus(resultJson);
            if ("1".equals(resultJson.getString("code"))) {
                // 探测成功，自动生成接口地址
                synchronized (this) { // 为避免并发带来问题先用线程锁
                    Map<String, String> interfaceMap = interfaceMapper.queryInterfaceByMethod(methodId);
                    String interfaceId = interfaceMap.get("ID");
                    String publishUrl = interfaceMap.get("PUBLISHURL");
                    String interfaceType = interfaceMap.get("INTERFACETYPE");
                    String areaCode = interfaceMap.get("AREACODE");
                    String interfaceUrl = interfaceMap.get("INTERFACEURL");
                    String tempUrl = "/" + areaCode + "_";
                    if (StringUtils.isBlank(publishUrl)) {
                        // 如果为空，则自动生成发布地址
                        String partUrl = interfaceUrl.substring(interfaceUrl.lastIndexOf("/") + 1, interfaceUrl.length());
                        if ("01".equals(interfaceType)) {
                            // webservice
                            if (!StringUtils.isBlank(partUrl)) {
                                if (partUrl.contains(".")) {
                                    partUrl = partUrl.substring(0, partUrl.lastIndexOf("."));
                                }
                                if (partUrl.contains("?")) {
                                    partUrl = partUrl.substring(0, partUrl.lastIndexOf("?"));
                                }
                            }
                        }
                        tempUrl = tempUrl + partUrl;
                        // 查询这个地址是不是已经存在
                        int existCount = interfaceMapper.queryPublishUrlExist(tempUrl);
                        if (existCount > 0) {
                            tempUrl = tempUrl + "_" + existCount;
                        }
                        // 保存这个url
                        interfaceMapper.updateInterfacePublishUrl(interfaceId, tempUrl);
                    }
                }
                // 启动动态发布该接口
                // 判断是http还是webservice
                Map<String, String> methodMap = interfaceMapper.queryMethodInfoByMethodId(methodId);
                String interfaceType = methodMap.get("INTERFACETYPE");
                String publishurl = methodMap.get("PUBLISHURL");
                String interfaceId = methodMap.get("INTERFACEID");
                boolean presult = false;
                if ("01".equals(interfaceType)) {
                    // webservice
                    presult = soapCreate.soapCreateGenerate(interfaceId);
                } else if ("02".equals(interfaceType)) {
                    // http
                    String requestType = methodMap.get("REQUESTTYPE");
                    presult = autoCreate.httpCreateGenerate(requestType, publishurl, interfaceId, 2);
                }
                if (presult) {
                    // 修改接口状态为待测试
                    interfaceMapper.updateInterfaceState(interfaceId, InterfacePublishStateEnum.WAITINGTEST.getCode());
                    JSONObject params = new JSONObject();
                    params.put("publishstate", InterfacePublishStateEnum.WAITINGTEST.getCode());
                    params.put("methodid", methodId);
                    interfaceMapper.updateMethodState(params);
                    logger.info("接口状态转为待测试，发布接口id:" + interfaceId + ",方法id:" + methodId);
                }
            }
        } catch (Exception e) {
            // 内部异常，可能接口参数不对
            logger.error("执行接口探测methodid:[" + methodId + "]出现错误，错误信息：" + ExceptionUtils.getStackTrace(e));
        }
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }
}
