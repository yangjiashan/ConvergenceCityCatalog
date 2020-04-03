package com.fgi.city.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class InterfaceCityBean {

    private String id;
    private String interface_name;
    private String interface_url;
    private String interface_desc;
    private String interface_type;
    private String request_type;
    private String param_format;
    private String methodid;
    private List<MethodFieldBean> input_parameters; // 输入参数
    private List<MethodFieldBean> out_parameters; // 输出参数
    private JSONObject result_example;

    public List<MethodFieldBean> getInput_parameters() {
        return input_parameters;
    }

    public void setInput_parameters(List<MethodFieldBean> input_parameters) {
        this.input_parameters = input_parameters;
    }

    public List<MethodFieldBean> getOut_parameters() {
        return out_parameters;
    }

    public void setOut_parameters(List<MethodFieldBean> out_parameters) {
        this.out_parameters = out_parameters;
    }

    public JSONObject getResult_example() {
        return result_example;
    }

    public void setResult_example(JSONObject result_example) {
        this.result_example = result_example;
    }

    public String getMethodid() {
        return methodid;
    }

    public void setMethodid(String methodid) {
        this.methodid = methodid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterface_name() {
        return interface_name;
    }

    public void setInterface_name(String interface_name) {
        this.interface_name = interface_name;
    }

    public String getInterface_url() {
        return interface_url;
    }

    public void setInterface_url(String interface_url) {
        this.interface_url = interface_url;
    }

    public String getInterface_desc() {
        return interface_desc;
    }

    public void setInterface_desc(String interface_desc) {
        this.interface_desc = interface_desc;
    }

    public String getInterface_type() {
        return interface_type;
    }

    public void setInterface_type(String interface_type) {
        this.interface_type = interface_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getParam_format() {
        return param_format;
    }

    public void setParam_format(String param_format) {
        this.param_format = param_format;
    }
}
