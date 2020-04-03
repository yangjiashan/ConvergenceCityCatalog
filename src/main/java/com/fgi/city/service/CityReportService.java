package com.fgi.city.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 目录、接口上报业务逻辑处理接口
 */
public interface CityReportService {

    /**
     * 根据机构名称获取数量
     * @param orgname
     * @return
     */
    int getCountByOrgName(String orgname);

    /**
     * 统一处理机构上报
     * @param data
     * @param result
     */
    void dealWithOrgReport(JSONObject data, JSONObject result);

    /**
     * 统一处理机构图标上报
     * @param data
     * @param result
     */
    void dealWithOrgIconReport(JSONObject data, JSONObject result);

    /**
     * 统一处理业务系统上报
     * @param data
     * @param result
     */
    void dealWithSysReport(JSONObject data, JSONObject result);


    /**
     * 统一处理业务事项上报
     * @param data
     * @param result
     */
    void dealWithCatalogReport(JSONObject data, JSONObject result);


    /**
     * 处理业务信息上报
     * @param data
     * @param result
     */
    void dealWithMatterReport(JSONObject data, JSONObject result);

    /**
     * 处理业务信息指标项上报
     * @param data
     * @param result
     */
    void dealWithMatterIndicatorReport(JSONObject data, JSONObject result);

    /**
     * 处理业务信息预览数据上报
     * @param data
     * @param result
     */
    void dealWithMatterDataReport(JSONObject data, JSONObject result);

    /**
     * 处理业务信息预览数据接口地址上报
     * @param data
     * @param result
     */
    void dealWithMatterDataUrlReport(JSONObject data, JSONObject result);

    /**
     * 处理地市接口上报
     * @param data
     * @param result
     */
    void dealWithInterfaceReport(JSONObject data, JSONObject result);

    /**
     * 处理地市接口方法上报
     * @param data
     * @param result
     */
    void dealWithInterfaceMethodReport(JSONObject data, JSONObject result);


    /**
     * 处理地市接口上报
     * @param data
     * @param result
     */
    void dealWithHttpInterfaceReport(JSONObject data, JSONObject result);

    /**
     * 处理地市账号密码上报
     * @param data
     * @param result
     */
    void dealWithAccountAndPwdReport(JSONObject data, JSONObject result);

    /**
     * 处理地市接口调用日志上报
     * @param data
     * @param result
     */
    void dealWithInterfaceLogReport(JSONObject data, JSONObject result);

}
