package com.fgi.city.Test;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.service.impl.HttpRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * http 测试
 */
@RestController
public class TestHttp {
    @Autowired
    private HttpRequestService httpRequestService;

    @RequestMapping("/testHttp123")
    public String testHttp(HttpServletRequest request){
//        JSONObject jsonData = httpRequestService.getRequestJsonData(request, new JSONObject());
//        String name = jsonData.getString("name");
//        String addr = jsonData.getString("addr");
        String name = request.getParameter("name");
        String addr = request.getParameter("addr");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("addr", addr);
        return jsonObject.toJSONString();
    }

}
