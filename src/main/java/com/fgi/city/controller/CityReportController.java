package com.fgi.city.controller;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.service.CityReportService;
import com.fgi.city.service.impl.HttpRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 地市上报目录、接口信息控制类
 */
@RestController
@RequestMapping("/report")
public class CityReportController {

    @Autowired
    private HttpRequestService httpRequestService;

    @Autowired
    private CityReportService cityReportService;

    /**
     * 机构上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/orgreport")
    public String orgReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject orgData = httpRequestService.getRequestJsonData(request, result);
        if (orgData != null) {
            // 处理机构上报
            cityReportService.dealWithOrgReport(orgData, result);
        }
        return result.toJSONString();
    }

    /**
     * 机构图标上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/iconreport")
    public String orgIconReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject orgIconData = httpRequestService.getRequestJsonData(request, result);
        if (orgIconData != null) {
            // 处理机构图标上报
            cityReportService.dealWithOrgIconReport(orgIconData, result);
        }
        return result.toJSONString();
    }

    /**
     * 业务系统上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/sysreport")
    public String sysReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject sysData = httpRequestService.getRequestJsonData(request, result);
        if (sysData != null) {
            // 处理业务系统上报
            cityReportService.dealWithSysReport(sysData, result);
        }
        return result.toJSONString();
    }

    /**
     * 业务事项上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/catalogreport")
    public String catalogReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject catalogData = httpRequestService.getRequestJsonData(request, result);
        if (catalogData == null)
            return result.toJSONString();
        // 处理业务事项上报
        cityReportService.dealWithCatalogReport(catalogData, result);
        return result.toJSONString();
    }

    /**
     * 业务信息上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/businessmatterreport")
    public String businessMatterReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject matterData = httpRequestService.getRequestJsonData(request, result);
        if (matterData != null) {
            // 处理业务信息上报
            cityReportService.dealWithMatterReport(matterData, result);
        }
        return result.toJSONString();
    }

    /**
     * 业务指标项上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/businessindicatorreport")
    public String businessIndicatorReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject indicatorData = httpRequestService.getRequestJsonData(request, result);
        if (indicatorData != null) {
            // 处理业务指标项上报
            cityReportService.dealWithMatterIndicatorReport(indicatorData, result);
        }
        return result.toJSONString();
    }

//    /**
//     * 业务信息预览数据上报
//     *
//     * @param request
//     * @return
//     */
//    @PostMapping(value = "/datareport")
//    public String businessDataReport(HttpServletRequest request) {
//        JSONObject result = new JSONObject();
//        // 读取数据转换json
//        JSONObject businessData = httpRequestService.getRequestJsonData(request, result);
//        if (businessData != null) {
//            // 处理业务信息预览数据上报
//            cityReportService.dealWithMatterDataReport(businessData, result);
//        }
//        return result.toJSONString();
//    }

    /**
     * 业务信息预览数据接口地址上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/datapreviewurlreport")
    public String businessDataUrlReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject businessData = httpRequestService.getRequestJsonData(request, result);
        if (businessData != null) {
            // 处理业务信息预览数据接口地址上报
            cityReportService.dealWithMatterDataUrlReport(businessData, result);
        }
        return result.toJSONString();
    }

    /**
     * 地市接口上报 (webservice)
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/webserviceinterfacereport")
    public String interfaceReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理地市接口上报(webservice)
            cityReportService.dealWithInterfaceReport(interfaceData, result);
        }
        return result.toJSONString();
    }

    /**
     * 地市接口方法上报 (webservice方法)
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/webserviceinterfacemethodreport")
    public String interfaceMethodReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理地市接口方法上报
            cityReportService.dealWithInterfaceMethodReport(interfaceData, result);
        }
        return result.toJSONString();
    }

    /**
     * 地市接口上报 (HTTP RESTful风格)
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/httpinterfacereport")
    public String httpInterfaceReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理地市接口上报(HTTP RESTful风格)
            cityReportService.dealWithHttpInterfaceReport(interfaceData, result);
        }
        return result.toJSONString();
    }

    /**
     * 地市接口调用账号、密码上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/permissionsreport")
    public String httpPermissionsReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理地市接口调用账号、密码上报
            cityReportService.dealWithAccountAndPwdReport(interfaceData, result);
        }
        return result.toJSONString();
    }

    /**
     * 地市接口调用日志上报
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/interfacelog")
    public String interfaceLogReport(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理地市接口调用日志上报
            cityReportService.dealWithInterfaceLogReport(interfaceData, result);
        }
        return result.toJSONString();
    }
}
