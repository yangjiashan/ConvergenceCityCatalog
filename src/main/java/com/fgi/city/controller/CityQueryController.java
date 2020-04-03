package com.fgi.city.controller;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.service.CityQueryService;
import com.fgi.city.service.impl.HttpRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 省平台下发接口控制类
 */
@RestController
@RequestMapping("/query")
public class CityQueryController {

    @Autowired
    private HttpRequestService httpRequestService;

    @Autowired
    private CityQueryService cityQueryService;

    /**
     * 下发接口列表
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/interfacelist")
    public String queryInterfaceList(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理接口目录下发
            cityQueryService.queryInterfaceList(interfaceData, result);
        }
        return result.toJSONString();
    }


    /**
     * 下发接口信息
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/interfaceinfo")
    public String queryInterfaceInfo(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理接口信息下发
            cityQueryService.queryInterfaceInfo(interfaceData, result);
        }
        return result.toJSONString();
    }


    /**
     * 下发接口方法
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/interfacemethod")
    public String queryInterfaceMethodList(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理接口方法信息下发
            cityQueryService.queryInterfaceMethodList(interfaceData, result);
        }
        return result.toJSONString();
    }


    /**
     * 下发接口接口调用日志
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/interfacelog")
    public String queryInterfaceLog(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        // 读取数据转换json
        JSONObject interfaceData = httpRequestService.getRequestJsonData(request, result);
        if (interfaceData != null) {
            // 处理接口日志信息下发
            cityQueryService.queryInterfaceLog(interfaceData, result);
        }
        return result.toJSONString();
    }
}
