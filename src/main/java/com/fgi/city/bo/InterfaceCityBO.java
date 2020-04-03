package com.fgi.city.bo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fgi.city.entity.MethodFieldBean;

import java.util.List;

public class InterfaceCityBO {
    @JSONField(ordinal = 1)
    private String interface_name;
    @JSONField(ordinal = 2)
    private String interface_url;
    @JSONField(ordinal = 3)
    private String interface_desc;
    @JSONField(ordinal = 4)
    private String interface_type;
    @JSONField(ordinal = 5)
    private String request_type;
    @JSONField(ordinal = 6)
    private String param_format;
    @JSONField(ordinal = 7)
    private List<MethodFieldBean> input_parameters; // 输入参数
    @JSONField(ordinal = 8)
    private List<MethodFieldBean> out_parameters; // 输出参数
    @JSONField(ordinal = 9)
    private JSONObject result_example;

    public InterfaceCityBO() {
    }

    public InterfaceCityBO(String interface_name, String interface_url, String interface_desc, String interface_type, String request_type, String param_format) {
        this.interface_name = interface_name;
        this.interface_url = interface_url;
        this.interface_desc = interface_desc;
        this.interface_type = interface_type;
        this.request_type = request_type;
        this.param_format = param_format;
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
}
