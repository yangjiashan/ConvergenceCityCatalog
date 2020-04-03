package com.fgi.city.service;

import com.alibaba.fastjson.JSONObject;

public interface CityQueryService {

    /**
     * 查询接口列表
     *
     * @param jsonData
     * @param result
     */
    void queryInterfaceList(JSONObject jsonData, JSONObject result);

    /**
     * 查询接口方法信息
     *
     * @param jsonData
     * @param result
     */
    void queryInterfaceMethodList(JSONObject jsonData, JSONObject result);

    /**
     * 查询接口日志信息
     *
     * @param jsonData
     * @param result
     */
    void queryInterfaceLog(JSONObject jsonData, JSONObject result);

    /**
     * 查询接口信息
     *
     * @param jsonData
     * @param result
     */
    void queryInterfaceInfo(JSONObject jsonData, JSONObject result);
}
