package com.fgi.city.template;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.client.ClientHandler;
import com.fgi.city.client.HttpClientHandler;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.entity.ApiSequence;
import com.fgi.city.entity.InterfacePermissionBean;
import com.fgi.city.enums.InterfacePublishStateEnum;
import com.fgi.city.enums.ParamFormatEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.service.impl.CityQueryServiceImpl;
import com.fgi.city.service.impl.HttpRequestService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * http接口模板
 */
@RestController
//@RequestMapping("template_class_mapper")
public class Template_get {
    private InterfaceMapper interfaceMapper = BeanProvider.getBean(InterfaceMapper.class);
    private RequestTools requestTools = BeanProvider.getBean(RequestTools.class);
    private HttpRequestService httpRequestService = BeanProvider.getBean(HttpRequestService.class);
    private transient org.slf4j.Logger logger = LoggerFactory.getLogger(CityQueryServiceImpl.class);

    /**
     * 地市的接口参数形式是 get方式
     *
     * @return
     */
    @RequestMapping(value = "template_method_mapper", method = RequestMethod.POST)
    public String template_method_name(HttpServletRequest request) {
        String interfaceId = "template_interface_id";
        JSONObject result = new JSONObject();
        try {
            // 根据接口id查询方法ID（接口和方法关系是一对一的 ，为了兼容webservice 将http接口拆分为接口和方法信息）
            Map<String, String> interfaceMethod = interfaceMapper.queryMethodByInterfaceId(interfaceId);
            String paramformat = interfaceMethod.get("PARAMFORMAT"); // 参数格式，01:json格式字符串，02:键-值&分隔形式
            String methodId = interfaceMethod.get("METHODID");
            JSONObject jsonData = null;
            String guid = "";
            String requestParamType = "";
            if (ParamFormatEnum.JSON_FORMAT.getVal().equals(paramformat)) {
                // json格式（附加在body中的json格式字符串）
                jsonData = httpRequestService.getRequestJsonData(request, new JSONObject());
                if (jsonData == null) {
                    // 读取参数错误，返回
                    result.put("code", ResultStatusEnum.FAILURE306.getCode());
                    result.put("message", ResultStatusEnum.FAILURE306.getDesc());
                    return result.toJSONString();
                }
                guid = jsonData.getString("guid");
                requestParamType = "1";
            } else if (ParamFormatEnum.KV_FORMAT.getVal().equals(paramformat)) {
                // 键值对格式
                guid = request.getParameter("guid");
                logger.info("guid:"+guid);
                requestParamType = "0";
            } else {
                // 内部异常
                result.put("code", ResultStatusEnum.FAILURE300.getCode());
                result.put("message", ResultStatusEnum.FAILURE300.getDesc());
                return result.toJSONString();
            }
            // 判断guid是否有权限
            Map<String, InterfacePermissionBean> map = new HashMap<>();
            if (!requestTools.validatePermission(guid, methodId, result, map)) {
                // guid验证不通过或者没有权限，不加日志
                return result.toJSONString();
            }
            // 判断接口是否是禁用（只有接口发布状态为开启的接口才能正常访问）
            String interfacepublishstate = interfaceMethod.get("INTERFACE_PUBLISHSTATE");
            String methodpublishstate = interfaceMethod.get("METHOD_PUBLISHSTATE");
            if (InterfacePublishStateEnum.DISABLE.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.DISABLE.getCode().equals(methodpublishstate)) {
                //接口被禁用
                result.put("code", ResultStatusEnum.FAILURE307.getCode());
                result.put("message", ResultStatusEnum.FAILURE307.getDesc());
                return result.toJSONString();
            } else if (InterfacePublishStateEnum.NOPUBLISH.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.NOPUBLISH.getCode().equals(methodpublishstate)
            ) {
                // 该接口还未封装
                result.put("code", ResultStatusEnum.FAILURE308.getCode());
                result.put("message", ResultStatusEnum.FAILURE308.getDesc());
                return result.toJSONString();
            } else if (InterfacePublishStateEnum.UPDATENODEAL.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.UPDATENODEAL.getCode().equals(methodpublishstate) ||
                    InterfacePublishStateEnum.DELETENODEAL.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.DELETENODEAL.getCode().equals(methodpublishstate)
            ) {
                // 该接口已被更新，等待重新封装
                result.put("code", ResultStatusEnum.FAILURE309.getCode());
                result.put("message", ResultStatusEnum.FAILURE309.getDesc());
                return result.toJSONString();
            } else if (InterfacePublishStateEnum.DELETED.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.DELETED.getCode().equals(methodpublishstate)) {
                // 该接口已被删除
                result.put("code", ResultStatusEnum.FAILURE311.getCode());
                result.put("message", ResultStatusEnum.FAILURE311.getDesc());
                return result.toJSONString();
            }
//            // 判断接口是是否是启用状态
//            if (!InterfacePublishStateEnum.ENABLE.getCode().equals(interfacepublishstate) ||
//                    !InterfacePublishStateEnum.ENABLE.getCode().equals(methodpublishstate)) {
//                // 该接口还没有被启动
//                result.put("code", ResultStatusEnum.FAILURE305.getCode());
//                result.put("message", ResultStatusEnum.FAILURE305.getDesc());
//                return result.toJSONString();
//            }

            // 修改2019/11/7
            // 根据方法ID获取账号密码信息 （同一个areacode 账号、密码、口令地址相同）
            Map<String, String> accountInfoMap = interfaceMapper.queryAccountAndPwdByMethodID(methodId);
            if (accountInfoMap == null) {
                accountInfoMap = new HashMap<>();
            }

            // 填充参数
            JSONObject paramObject = requestTools.setParams(methodId, null, accountInfoMap, 1, request, jsonData, paramformat);
            logger.info(paramObject.toString());

            // 组装请求
            ApiSequence apiSequence = new ApiSequence();
            // 请求方式 get/post/put..
            apiSequence.setApimethod(interfaceMethod.get("REQUESTTYPE"));
            apiSequence.setUrl(interfaceMethod.get("INTERFACEURL"));
            // 暂时用10秒
            apiSequence.setMaxconnectionseconds(ClientHandler.maxConnectionSeconds);
            apiSequence.setParamters(paramObject.toJSONString());
            // 没用到
            apiSequence.setCondition("");
            // 设置参数组装类型，0-键值对，1-json     库里保存 参数格式，01:json格式字符串，02:键-值&分隔形式
            apiSequence.setParamstype(requestParamType);
            HttpClientHandler httpClientHandler = new HttpClientHandler(apiSequence);
            httpClientHandler.execute();
            System.out.println(httpClientHandler.getStatusCode());
            System.out.println(httpClientHandler.getBody());
            // 加入日志
            requestTools.addInvokeLog(map.get("permission"), interfaceMethod, interfaceId, methodId);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
            result.put("content", httpClientHandler.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            // 内部异常
            logger.error(ExceptionUtils.getMessage(e));
            result.put("code", ResultStatusEnum.FAILURE300.getCode());
            result.put("message", ResultStatusEnum.FAILURE300.getDesc());
        }
        return result.toJSONString();
    }
}

