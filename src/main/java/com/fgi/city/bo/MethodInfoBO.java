package com.fgi.city.bo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fgi.city.entity.MethodFieldBean;

import java.util.List;

public class MethodInfoBO {
    @JSONField(ordinal = 1)
    private String method_ename;
    @JSONField(ordinal = 2)
    private String method_cname;
    @JSONField(ordinal = 3)
    private String method_type;
    @JSONField(ordinal = 4)
    private String method_desc;
    @JSONField(ordinal = 5)
    private String relationship;
    @JSONField(ordinal = 6)
    private List<MethodFieldBean> input_parameters; // 输入参数
    @JSONField(ordinal = 7)
    private List<MethodFieldBean> out_parameters; // 输出参数
    @JSONField(ordinal = 8)
    private JSONObject result_example;
    private String resultexample;
    private String createtime;
    private String updatetime;
    private String querytable;
    private String requesttype;
    private String paramformat;
    private String id;

    public MethodInfoBO() {
    }

    public MethodInfoBO(String method_ename, String method_cname, String method_type, String method_desc, String relationship) {
        this.method_ename = method_ename;
        this.method_cname = method_cname;
        this.method_type = method_type;
        this.method_desc = method_desc;
        this.relationship = relationship;
    }

    public String getMethod_ename() {
        return method_ename;
    }

    public void setMethod_ename(String method_ename) {
        this.method_ename = method_ename;
    }

    public String getMethod_cname() {
        return method_cname;
    }

    public void setMethod_cname(String method_cname) {
        this.method_cname = method_cname;
    }

    public String getMethod_type() {
        return method_type;
    }

    public void setMethod_type(String method_type) {
        this.method_type = method_type;
    }

    public String getMethod_desc() {
        return method_desc;
    }

    public void setMethod_desc(String method_desc) {
        this.method_desc = method_desc;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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

    public String getResultexample() {
        return resultexample;
    }

    public void setResultexample(String resultexample) {
        this.resultexample = resultexample;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getQuerytable() {
        return querytable;
    }

    public void setQuerytable(String querytable) {
        this.querytable = querytable;
    }

    public String getRequesttype() {
        return requesttype;
    }

    public void setRequesttype(String requesttype) {
        this.requesttype = requesttype;
    }

    public String getParamformat() {
        return paramformat;
    }

    public void setParamformat(String paramformat) {
        this.paramformat = paramformat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
