package com.fgi.city.entity;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author sirc_yjs
 * @Description 接口信息
 * @date 2019年2月21日
 */
public class ApiSequence {

    private String id;
    private String url; // 接口地址
    private String apimethod; // 接口类型（GET, HEAD, POST, PUT, DELETE,WEBSERVICE）
    private String paramstype; // 参数格式类型， 0键值对形式，1json格式
    private String headers; // 请求头部，格式（key::value\\nkey::value）
    private String paramters; // 请求参数，格式（key::value\\nkey::value）
    private String maxconnectionseconds; // 最大连接时间
    private String conditiontype = "CONTAINS"; // 结果校验类型（CONTAINS, DOESNT_CONTAIN, STATUSCODE, DEFAULT）
    private String condition; // 结果校验内容

    private HashMap<String, String> headersMap = new LinkedHashMap<String, String>();//请求头部
    private HashMap<String, String> parametersMap = new LinkedHashMap<String, String>();//请求参数

    private String urlmethod; // 接口地址对应方法
    private String targetspace; // 接口对应目标空间

    private String soapaction; // soapurl请求地址
    private String methodtype; // 方法类型（方法类型：0、查询类，1、验证类）

    public String getMethodtype() {
        return methodtype;
    }

    public String getParamstype() {
        return paramstype;
    }

    public void setParamstype(String paramstype) {
        this.paramstype = paramstype;
    }

    public void setMethodtype(String methodtype) {
        this.methodtype = methodtype;
    }

    public String getSoapaction() {
        return soapaction;
    }

    public void setSoapaction(String soapaction) {
        this.soapaction = soapaction;
    }

    public String getUrlmethod() {
        return urlmethod;
    }

    public void setUrlmethod(String urlmethod) {
        this.urlmethod = urlmethod;
    }

    public String getTargetspace() {
        return targetspace;
    }

    public void setTargetspace(String targetspace) {
        this.targetspace = targetspace;
    }

    public void setParamsToMap() {
        this.parametersMap = this.stringToMap(paramters);
//        this.headersMap = this.stringToMap(headers);
    }

    public String mapToString(HashMap<String, String> map) {
        if (map == null || map.size() == 0) return null;
        StringBuffer sb = new StringBuffer();
        for (String key : map.keySet()) {
            if (sb.length() != 0) sb.append("\r\n");
            sb.append(key).append("::").append(map.get(key));
        }
        return sb.toString();
    }

    public HashMap<String, String> stringToMap(String text) {
        HashMap<String, String> map = new LinkedHashMap<String, String>();
        JSONObject jsonObject = JSONObject.parseObject(text);
        for (String str : jsonObject.keySet()) {
            map.put(str, jsonObject.getString(str));
        }
        return map;
    }

    public HashMap<String, String> getHeadersMap() {
        return headersMap;
    }

    public void setHeadersMap(HashMap<String, String> headersMap) {
        this.headersMap = headersMap;
    }

    public HashMap<String, String> getParametersMap() {
        return parametersMap;
    }

    public void setParametersMap(HashMap<String, String> parametersMap) {
        this.parametersMap = parametersMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApimethod() {
        return apimethod;
    }

    public void setApimethod(String apimethod) {
        this.apimethod = apimethod;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getParamters() {
        return paramters;
    }

    public void setParamters(String paramters) {
        this.paramters = paramters;
    }

    public String getMaxconnectionseconds() {
        return maxconnectionseconds;
    }

    public void setMaxconnectionseconds(String maxconnectionseconds) {
        this.maxconnectionseconds = maxconnectionseconds;
    }

    public String getConditiontype() {
        return conditiontype;
    }

    public void setConditiontype(String conditiontype) {
        this.conditiontype = conditiontype;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}
