package com.fgi.city.template;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.client.ClientHandler;
import com.fgi.city.client.WebserviceClientHandler;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.entity.ApiSequence;
import com.fgi.city.entity.InterfacePermissionBean;
import com.fgi.city.enums.InterfacePublishStateEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.service.impl.CityQueryServiceImpl;
import com.fgi.city.utils.CxfMessageUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.HashMap;
import java.util.Map;

@Component
@WebService(
        targetNamespace = "http://tempuri.org/")
public class TestApiService {
    private transient org.slf4j.Logger logger = LoggerFactory.getLogger(CityQueryServiceImpl.class);
    private InterfaceMapper interfaceMapper = BeanProvider.getBean(InterfaceMapper.class);
    private CxfMessageUtil cxfMessageUtil = new CxfMessageUtil();
    private RequestTools requestTools = BeanProvider.getBean(RequestTools.class);

    /**
     * 动态封装可以跳过获取token的接口，获取token的接口不需要对地市公开，都是我们自己调用
     * webservice动态封装局限性,返回格式不能做到透明
     * <response><content>...</content></response>
     *
     * @param username
     * @param password
     * @return
     */
    @WebMethod
    public String insertPersonInfo(@WebParam(name = "guid") String guid, @WebParam(name = "username") String username, @WebParam(name = "password") String password) {
//        String interfaceId = "template_interface_id";
        String interfaceId = "32456789098765";
//        String methodId = "template_method_id";
        String methodId = "4444";
        JSONObject result = new JSONObject();
        Document responseDoc = DocumentHelper.createDocument();
        Element responseElement = responseDoc.addElement("Response");
        Element responseCode = responseElement.addElement("code");
        Element responseMessage = responseElement.addElement("message");
        responseCode.setText(ResultStatusEnum.SUCCUSS.getCode());
        responseMessage.setText(ResultStatusEnum.SUCCUSS.getDesc());
        Map<String, InterfacePermissionBean> map = new HashMap<>();
        if (!requestTools.validatePermission(guid, methodId, result, map)) {
            // guid验证不通过或者没有权限，不加日志
            responseCode.setText(result.getString("code"));
            responseMessage.setText(result.getString("message"));
            return responseDoc.asXML();
        }
        try {
            Message message = PhaseInterceptorChain.getCurrentMessage();
            MessageContentsList messageContentsList = cxfMessageUtil.getContentsList(message);
            // 根据方法ID获取接口信息
            Map<String, String> methodMap = interfaceMapper.queryMethodInfoByMethodId(methodId);
            // 判断接口是否是禁用
            // 接口目前状态共有：0->未发布   1->待测试    2->已测试   3->已更新未处理  4->已删除未处理  5->已删除  6->禁用  7->启用
            String interfacepublishstate = methodMap.get("INTERFACEPUBLISHSTATE");
            String methodpublishstate = methodMap.get("METHODPUBLISHSTATE");
            if (InterfacePublishStateEnum.DISABLE.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.DISABLE.getCode().equals(methodpublishstate)) {
                //接口被禁用
                responseCode.setText(ResultStatusEnum.FAILURE307.getCode());
                responseMessage.setText(ResultStatusEnum.FAILURE307.getDesc());
                return responseDoc.asXML();
            } else if (InterfacePublishStateEnum.NOPUBLISH.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.NOPUBLISH.getCode().equals(methodpublishstate)
            ) {
                // 该接口还未封装
                responseCode.setText(ResultStatusEnum.FAILURE308.getCode());
                responseMessage.setText(ResultStatusEnum.FAILURE308.getDesc());
                return responseDoc.asXML();
            } else if (InterfacePublishStateEnum.UPDATENODEAL.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.UPDATENODEAL.getCode().equals(methodpublishstate) ||
                    InterfacePublishStateEnum.DELETENODEAL.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.DELETENODEAL.getCode().equals(methodpublishstate)
            ) {
                // 该接口已被更新，等待重新封装
                responseCode.setText(ResultStatusEnum.FAILURE309.getCode());
                responseMessage.setText(ResultStatusEnum.FAILURE309.getDesc());
                return responseDoc.asXML();
            } else if (InterfacePublishStateEnum.DELETED.getCode().equals(interfacepublishstate) ||
                    InterfacePublishStateEnum.DELETED.getCode().equals(methodpublishstate)) {
                // 该接口已被删除
                responseCode.setText(ResultStatusEnum.FAILURE311.getCode());
                responseMessage.setText(ResultStatusEnum.FAILURE311.getDesc());
                return responseDoc.asXML();
            }
//            // 判断接口是是否是启用状态
//            if (!InterfacePublishStateEnum.ENABLE.getCode().equals(interfacepublishstate) ||
//                    !InterfacePublishStateEnum.ENABLE.getCode().equals(methodpublishstate)) {
//                // 该接口还没有被启动
//                responseCode.setText(ResultStatusEnum.FAILURE305.getCode());
//                responseMessage.setText(ResultStatusEnum.FAILURE305.getDesc());
//                return responseDoc.asXML();
//            }

            // 修改2019/11/7
            // 根据方法ID获取账号密码信息 （同一个areacode 账号、密码、口令地址相同）
            Map<String, String> accountInfoMap = interfaceMapper.queryAccountAndPwdByMethodID(methodId);
            if (accountInfoMap == null) {
                accountInfoMap = new HashMap<>();
            }

            JSONObject paramObject = requestTools.setParams(methodId, messageContentsList, accountInfoMap, 0, null, null, null);
            // 组装请求
            ApiSequence apiSequence = new ApiSequence();
            apiSequence.setApimethod("WEBSERVICE");
            apiSequence.setUrl(methodMap.get("INTERFACEURL"));
            // 暂时用10秒
            apiSequence.setMaxconnectionseconds(ClientHandler.maxConnectionSeconds);
            apiSequence.setParamters(paramObject.toJSONString());
            apiSequence.setUrlmethod(methodMap.get("METHODNAME"));
            apiSequence.setMethodtype(methodMap.get("METHODTYPE"));
            // 没用到
            apiSequence.setCondition("");
            WebserviceClientHandler webserviceClientHandler = new WebserviceClientHandler(apiSequence);
            webserviceClientHandler.execute();
            // 调用成功
            responseCode.setText(ResultStatusEnum.SUCCUSS.getCode());
            responseMessage.setText(ResultStatusEnum.SUCCUSS.getDesc());
            webserviceClientHandler.getResultStr(webserviceClientHandler.getBody(), responseElement);
            // 加入日志
            requestTools.addInvokeLog(map.get("permission"), methodMap, interfaceId, methodId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(ExceptionUtils.getMessage(e));
            responseCode.setText(ResultStatusEnum.FAILURE300.getCode());
            responseMessage.setText(ResultStatusEnum.FAILURE300.getDesc());
        }
        logger.info("======记录下接口调用结果=======：" + responseDoc.asXML());
        return responseDoc.asXML();

//        Document responseDoc = DocumentHelper.createDocument();
//        Element responseElement = responseDoc.addElement("Response");
//        Element responseCode = responseElement.addElement("username");
//        Element responseMessage = responseElement.addElement("password");
//        responseCode.setText(username);
//        responseMessage.setText(password);
//        return responseDoc.asXML();

    }
}
