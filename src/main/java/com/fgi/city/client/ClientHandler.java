package com.fgi.city.client;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.bo.MethodFieldBO;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.entity.ApiSequence;
import com.fgi.city.enums.ApiMethodEnum;
import com.fgi.city.enums.FieldTypeEnum;
import com.fgi.city.template.RequestTools;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class ClientHandler {

    private ApiSequence apisequence;
    private String methodId;
    public static String maxConnectionSeconds = "10";
    private InterfaceMapper interfaceMapper = BeanProvider.getBean(InterfaceMapper.class);
    private RequestTools requestTools = BeanProvider.getBean(RequestTools.class);

    public ClientHandler(String methodId) {
        this.methodId = methodId;
    }

    /**
     * 执行接口探测（http、webservice）
     *
     * @return
     */
    public String execute() throws RuntimeException {
        JSONObject result = new JSONObject();
        apisequence = queryMethodInfoByMethodID(methodId);

        String apiMethodStr = apisequence.getApimethod(); // GET, HEAD, POST, PUT, DELETE, WEBSERVICE
        String url = apisequence.getUrl(); // http://localhost:10003/ws/WsOkController?wsdl
        String urlMethod = apisequence.getUrlmethod(); // helloGoods
        String maxConnectionTime = apisequence.getMaxconnectionseconds(); // 超时
        String paramters = apisequence.getParamters(); // json格式参数
        String paramsType = apisequence.getParamstype(); // 0键值对， 1json
        String conditionType = apisequence.getConditiontype(); // CONTAINS、DOESNT_CONTAIN、STATUSCODE 默认STATUSCODE
        String conditions = apisequence.getCondition(); // 返回内容对比值
        apisequence.getMethodtype(); // 查询类、验证类

        // 设置默认超时时间
        if (StringUtils.isBlank(maxConnectionTime)) {
            apisequence.setMaxconnectionseconds(maxConnectionSeconds);
        }
        ApiMethodEnum apiMethodEnum = ApiMethodEnum.valueOf(apiMethodStr);
        // 如果是 webservice
        if (ApiMethodEnum.WEBSERVICE.equals(apiMethodEnum)) {
            // 验证webservice必填参数 必填 url、urlmethod、paramters
            if (StringUtils.isBlank(url) || StringUtils.isBlank(urlMethod)
                    || StringUtils.isBlank(paramters) || StringUtils.isBlank(conditionType)
                    || conditions == null) {
                result.put("code", "-1");
                result.put("message", "error 必填项缺失");
                return result.toJSONString();
            }
            // 请求webservice
            WebserviceClientHandler webserviceClientHandler = new WebserviceClientHandler(apisequence);
            webserviceClientHandler.execute();
            if (StringUtils.isBlank(webserviceClientHandler.getOutput())) {
                // 成功
                result.put("code", "1");
                result.put("message", "探测成功");
            } else {
                // 失败
                result.put("code", "-1");
                result.put("message", "探测失败；-失败原因->" + webserviceClientHandler.getOutput());
            }
            return result.toJSONString();
        } else {
            // 验证http必填参数 必填 url、paramstype 、paramters
            if (StringUtils.isBlank(url) || StringUtils.isBlank(paramsType)
                    || StringUtils.isBlank(paramters) || StringUtils.isBlank(conditionType)
                    || conditions == null) {
                result.put("code", "-1");
                result.put("message", "error 必填项缺失");
                return result.toJSONString();
            }
            // 请求http
            HttpClientHandler httpClientHandler = new HttpClientHandler(apisequence);
            httpClientHandler.execute();
            if (!StringUtils.isBlank(httpClientHandler.getOutput())) {
                // 失败
                result.put("code", "-1");
                result.put("message", "探测失败；-失败原因->" + httpClientHandler.getOutput());
            } else {
                // 成功
                result.put("code", "1");
                result.put("message", "探测成功");
            }
            return result.toJSONString();
        }
    }

    // 根据方法ID获取方法信息
    private ApiSequence queryMethodInfoByMethodID(String methodId) throws RuntimeException {
        Map<String, String> methodMap = interfaceMapper.queryMethodInfoByMethodId(methodId);
        ApiSequence apiSequence = new ApiSequence();
        String interfaceType = methodMap.get("INTERFACETYPE");
        String requestType = methodMap.get("REQUESTTYPE");
        String apiMethod = "";
        if ("01".equals(interfaceType)) {
            // WEBSERVICE
            apiMethod = ApiMethodEnum.WEBSERVICE.toString();
        } else {
            if ("GET".equals(requestType)) {
                // GET
                apiMethod = ApiMethodEnum.GET.toString();
            } else if ("HEAD".equals(requestType)) {
                // HEAD
                apiMethod = ApiMethodEnum.HEAD.toString();
            } else if ("POST".equals(requestType)) {
                // POST
                apiMethod = ApiMethodEnum.POST.toString();
            } else if ("PUT".equals(requestType)) {
                // PUT
                apiMethod = ApiMethodEnum.PUT.toString();
            } else if ("DELETE".equals(requestType)) {
                // DELETE
                apiMethod = ApiMethodEnum.DELETE.toString();
            } else {
                throw new RuntimeException("接口类型错误，GET/POST/WEBSERVICE/...");
            }
        }
        apiSequence.setApimethod(apiMethod);
        apiSequence.setUrl(methodMap.get("INTERFACEURL"));
        apiSequence.setUrlmethod(methodMap.get("METHODNAME"));
        apiSequence.setMethodtype(methodMap.get("METHODTYPE"));
        apiSequence.setMaxconnectionseconds(maxConnectionSeconds);
        if ("01".equals(methodMap.get("PARAMFORMAT"))) {
            // json格式
            apiSequence.setParamstype("1");
        } else if ("02".equals(methodMap.get("PARAMFORMAT"))) {
            // 键值对格式
            apiSequence.setParamstype("0");
        } else {
            throw new RuntimeException("参数格式错误，json/键值对");
        }
        apiSequence.setCondition("CONTAINS"); // 默认为包含
        // 根据方法ID获取账号密码信息
        Map<String, String> accountMap = interfaceMapper.queryAccountAndPwdByMethodID(methodId);
        // 根据方法ID找到对应的输入、输出示例值
        List<MethodFieldBO> inputParams = interfaceMapper.queryFieldByMethodID(methodId, "0");
        List<MethodFieldBO> outputParams = interfaceMapper.queryFieldByMethodID(methodId, "1");
        JSONObject inputJson = new JSONObject();
        String outputStr = "";
        for (MethodFieldBO mb : inputParams) {
            // 参数类型，01:账号类型，02:密码类型，03:口令类型，00：其他
            String paramType = mb.getParamtype();
            String originalVal = mb.getParamvalue();
            if (FieldTypeEnum.ACCOUNT_TYPE.getVal().equals(paramType)) {
                // 如果是账号类型，就替换成省平台的账号
                originalVal = accountMap.get("USERNAME");
            } else if (FieldTypeEnum.TOKEN_TYPE.getVal().equals(paramType)) {
                // 如果是口令类型，根据账号密码获取地市口令
                String areacode = accountMap.get("AREACODE");
                JSONObject jsonResult = requestTools.invokePermission(accountMap.get("USERNAME"), accountMap.get("PASSWORD"), areacode);
                if (jsonResult == null) {
                    // 获取口令失败, 抛出内部异常, 获取口令的接口还未开发
                    throw new RuntimeException("获取口令的接口还未开发!");
                } else {
                    String guidStr = jsonResult.getString("guid");
                    if (StringUtils.isBlank(guidStr)) {
                        // 获取口令异常，记录异常信息
                        String messageStr = jsonResult.getString("message");
                        throw new RuntimeException("获取口令异常-->" + messageStr);
                    } else {
                        originalVal = guidStr;
                    }
                }
            } else if (FieldTypeEnum.PASSWORD_TYPE.getVal().equals(paramType)) {
                // 如果是密码类型，就替换成省平台的密码
                originalVal = accountMap.get("PASSWORD");
            }
            inputJson.put(mb.getParamname(), originalVal);
        }
        // 寻找最长的输出参数作为返回值对比值
        for (MethodFieldBO mb : outputParams) {
            String tempVal = mb.getParamvalue();
            if (tempVal != null) {
                if (tempVal.length() > outputStr.length()) {
                    outputStr = tempVal;
                }
            }
        }
        if ("".equals(outputStr)) {
            // 数据异常
            throw new RuntimeException("返回值实例缺失，不能作对比...");
        }
        apiSequence.setParamters(inputJson.toJSONString());
        apiSequence.setCondition(outputStr); // 设置内容对比值
        return apiSequence;
    }

//    /**
//     * 执行接口
//     *
//     * @return
//     */
//    public String execute2(String methodId) throws RuntimeException {
//        apisequence = queryMethodInfoByMethodID2(methodId);
//
//        String apiMethodStr = apisequence.getApimethod(); // GET, HEAD, POST, PUT, DELETE, WEBSERVICE
//        String url = apisequence.getUrl(); // http://localhost:10003/ws/WsOkController?wsdl
//        String urlMethod = apisequence.getUrlmethod(); // helloGoods
//        String maxConnectionTime = apisequence.getMaxconnectionseconds(); // 10超时
//        String paramters = apisequence.getParamters(); // params::aa
//        String paramsType = apisequence.getParamstype(); // 0键值对， 1json
//        String conditionType = apisequence.getConditiontype(); // CONTAINS、DOESNT_CONTAIN、STATUSCODE 默认STATUSCODE
//        String conditions = apisequence.getCondition(); // 返回内容对比值
//        apisequence.getMethodtype(); // 查询类、验证类
//
//        // 设置默认超时时间
//        if (StringUtils.isBlank(maxConnectionTime)) {
//            apisequence.setMaxconnectionseconds(maxconnectionseconds);
//        }
//        ApiMethodEnum apiMethodEnum = ApiMethodEnum.valueOf(apiMethodStr);
//        // 如果是 webservice
//        if (ApiMethodEnum.WEBSERVICE.equals(apiMethodEnum)) {
//            // 验证webservice必填参数 必填 url、urlmethod、paramters
//            if (StringUtils.isBlank(url) || StringUtils.isBlank(urlMethod)
//                    || StringUtils.isBlank(paramters) || StringUtils.isBlank(conditionType)
//                    || conditions == null) {
//                return "error 必填项缺失";
//            }
//            // 请求webservice
//            WebserviceClientHandler webserviceClientHandler = new WebserviceClientHandler(apisequence);
//            webserviceClientHandler.execute();
//            return StringUtils.isBlank(webserviceClientHandler.getOutput()) ? "success" : webserviceClientHandler.getOutput();
//        } else {
//            // 验证http必填参数 必填 url、paramstype 、paramters
//            if (StringUtils.isBlank(url) || StringUtils.isBlank(paramsType)
//                    || StringUtils.isBlank(paramters) || StringUtils.isBlank(conditionType)
//                    || conditions == null) {
//                return "error 必填项缺失";
//            }
//            // 请求http
//            HttpClientHandler httpClientHandler = new HttpClientHandler(apisequence);
//            httpClientHandler.execute();
//            return StringUtils.isBlank(httpClientHandler.getOutput()) ? "success" : httpClientHandler.getOutput();
//        }
//    }
//
//    // 调用接口，自提供输入参数，暂时用作调用口令接口
//    public String execute(String methodId, String inputJson) {
//        Map<String, String> methodMap = interfaceMapper.queryMethodInfoByMethodId(methodId);
//        ApiSequence apiSequence = new ApiSequence();
//        String interfaceType = methodMap.get("INTERFACETYPE");
//        String requestType = methodMap.get("REQUESTTYPE");
//        apisequence.setApimethod("01".equals(interfaceType)?ApiMethodEnum.WEBSERVICE.toString():requestType);
//        apisequence.setUrl(methodMap.get("INTERFACEURL"));
//        apisequence.setUrlmethod(methodMap.get("METHODNAME"));
//        apisequence.setMethodtype(methodMap.get("METHODTYPE"));
//        apiSequence.setMaxconnectionseconds(maxconnectionseconds);
//        apisequence.setParamstype("01".equals(methodMap.get("PARAMFORMAT"))?"1":"0");
//        apisequence.setCondition("CONTAINS"); // 默认为包含
//        // 根据方法ID获取账号密码信息
//        Map<String, String> accountMap = interfaceMapper.queryAccountAndPwdByMethodID(methodId);
//        // 根据方法ID找到对应的输入、输出示例值
//        List<MethodFieldBO> inputParams = interfaceMapper.queryFieldByMethodID(methodId, "0");
//        apisequence.setParamters(inputJson);
//        // 如果是 webservice
//        if (ApiMethodEnum.WEBSERVICE.toString().equals(apisequence.getApimethod())) {
//            // 请求webservice
//            WebserviceClientHandler webserviceClientHandler = new WebserviceClientHandler(apisequence);
//            webserviceClientHandler.execute();
//        } else {
//            // 请求http
//            HttpClientHandler httpClientHandler = new HttpClientHandler(apisequence);
//            httpClientHandler.execute();
//        }
//        return apisequence;
//    }
}
