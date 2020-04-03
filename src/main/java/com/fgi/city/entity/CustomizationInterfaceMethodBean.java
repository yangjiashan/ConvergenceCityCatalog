package com.fgi.city.entity;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 接口方法实体类
 */
public class CustomizationInterfaceMethodBean {

    @JSONField(ordinal = 1)
    private String methodid;
    @JSONField(ordinal = 2)
    private String methodname;
    private String method_ename;
    private String method_cname;
    private String method_type;
    private String method_desc;
    private String relationship;
    private String interfacetype; // 接口类型，0表示自动生成接口，1表示定制接口，需要人工介入开发
    private String result_example; //返回值示例

    public String getResult_example() {
        return result_example;
    }

    public void setResult_example(String result_example) {
        this.result_example = result_example;
    }

    public String getInterfacetype() {
        return interfacetype;
    }

    public void setInterfacetype(String interfacetype) {
        this.interfacetype = interfacetype;
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

    public String getMethodid() {
        return methodid;
    }

    public void setMethodid(String methodid) {
        this.methodid = methodid;
    }

    public String getMethodname() {
        return methodname;
    }

    public void setMethodname(String methodname) {
        this.methodname = methodname;
    }
}
