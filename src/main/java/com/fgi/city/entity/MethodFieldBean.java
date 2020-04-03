package com.fgi.city.entity;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 方法参数实体类
 */
public class MethodFieldBean {

    private String id;
    private String methodid;
    private String fieldtype;
    @JSONField(ordinal = 2)
    private String param_ename;
    @JSONField(ordinal = 1)
    private String param_cname;
    private String param_name;
    private String param_desc;

    public String getParam_name() {
        return param_name;
    }

    public void setParam_name(String param_name) {
        this.param_name = param_name;
    }

    public String getParam_desc() {
        return param_desc;
    }

    public void setParam_desc(String param_desc) {
        this.param_desc = param_desc;
    }

    public String getMethodid() {
        return methodid;
    }

    public void setMethodid(String methodid) {
        this.methodid = methodid;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParam_ename() {
        return param_ename;
    }

    public void setParam_ename(String param_ename) {
        this.param_ename = param_ename;
    }

    public String getParam_cname() {
        return param_cname;
    }

    public void setParam_cname(String param_cname) {
        this.param_cname = param_cname;
    }
}
