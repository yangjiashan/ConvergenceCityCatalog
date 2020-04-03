package com.fgi.city.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * 接口实体类
 */
public class CustomizationInterfaceBean {

    private String id;
    @JSONField(ordinal = 1)
    private String interfaceid;
    @JSONField(ordinal = 2)
    private String interfacename;
    @JSONField(ordinal = 3)
    private String interface_url;
    @JSONField(ordinal = 4)
    private String interfacetype;
    @JSONField(ordinal = 5)
    private List<CustomizationInterfaceMethodBean> methods;

    public String getInterfacetype() {
        return interfacetype;
    }

    public void setInterfacetype(String interfacetype) {
        this.interfacetype = interfacetype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CustomizationInterfaceMethodBean> getMethods() {
        return methods;
    }

    public void setMethods(List<CustomizationInterfaceMethodBean> methods) {
        this.methods = methods;
    }

    public String getInterface_url() {
        return interface_url;
    }

    public void setInterface_url(String interface_url) {
        this.interface_url = interface_url;
    }

    public String getInterfaceid() {
        return interfaceid;
    }

    public void setInterfaceid(String interfaceid) {
        this.interfaceid = interfaceid;
    }

    public String getInterfacename() {
        return interfacename;
    }

    public void setInterfacename(String interfacename) {
        this.interfacename = interfacename;
    }
}
