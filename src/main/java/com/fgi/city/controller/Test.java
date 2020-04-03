package com.fgi.city.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fgi.city.template.Generate_Template_Http;
import com.fgi.city.template.Generate_Template_Soap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
public class Test {

    @Autowired
    private Generate_Template_Http autoCreate;

    @Autowired
    private Generate_Template_Soap soapCreate;

    @RequestMapping("/createUrl")
    public String createUrl(String interfaceId) {
        // 发布http
//        autoCreate.httpCreateGenerate("/goods", interfaceId, 2);
        // 发布webservice
        soapCreate.soapCreateGenerate(interfaceId);
//        try {
//            // 取消发布
//            soapCreate.publish(null, "/WsOkController", -1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return "success";
    }

    @RequestMapping("/cancelUrl")
    public String cancelUrl(String interfaceId) {
        // 取消发布
//        autoCreate.httpCreateGenerate("/goods", interfaceId, 3);

        try {
            // 取消发布
            soapCreate.publish(null, "/WsOkController", -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    @RequestMapping("/testHttp")
    public String testHttpGet(String params) {
        return params;
    }

    @RequestMapping(value = "/testHttpPost", method = RequestMethod.GET)
    public String testHttpPost(HttpServletRequest request) throws IOException {
        Map<String, String[]> map = request.getParameterMap();
        System.out.println(map);
        String params = request.getParameter("params");
        return params;
    }

    @RequestMapping("/testMatterData")
    public String testMatter(HttpServletRequest request){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject1.put("name","一号");
        jsonObject1.put("sex","女");
        jsonObject1.put("datetime","2019/11/14 17:52:11");

        jsonObject2.put("name","二号");
        jsonObject2.put("sex","女");
        jsonObject2.put("datetime","2019/11/14 17:52:11");
        jsonArray.add(jsonObject1);
        jsonArray.add(jsonObject2);
        return jsonArray.toJSONString();
    }

    // 是否影响调用的判断代码
    /*
    * (1) 如果是未封装的.... 没有影响
    *
    * (2) 已封装的，添加 接口（已经存在）、新增一个方法（当成首次上报处理）....  自动发布或者人工封装完配置  没有影响
    *     已封装的，编辑 判断是修改了什么参数  如果只是修改了（接口名称、接口描述、接口方法中文名称-简称
    *     、接口方法描述、接口查询业务表、排序号、参数描述） 则不影响（该干嘛干嘛），      其他则判断是影响（已更新未处理） --程序判断如果是可封装 在封装一次 -> 如果接口不可封装 则手动
    *     已封装的，删除 删除某个接口或者方法，影响  。 接口状态变为已删除未处理
    *
    *  有没有封装 怎么看状态 怎么根据状态判断接口是否封装
    *  未发布、待测试、已测试、启用、禁用、已删除、已删除，未处理、已更新，未处理
    *
    *  未发布（看成未封装的， 直接增删改）
    *  待测试、已测试、启用、禁用、已更新，未处理（看成是封装过的，需要判断操作是不是有影响）
    *
    *
    *  方法参数没有发布状态，当地市上报动作是删除时候并且对当前接口有影响时候，先是删除方法参数， 将方法、接口发布状态设置成为已删除未处理
    *  当又上报修改时候又改为已更新未处理
    *  判断修改时候有影响时候是拿接口或者方法的接口发布状态，所以接口、方法发布状态要同步，不能出现接口发布状态为未发布，方法发布状态为待测试
    *
    *
    *  接口探测 自动生成发布地址，探测成功，调起一次重新封装。。。
    *
    * */

}
