package com.fgi.city.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fgi.city.entity.CustomizationInterfaceBean;

import java.util.List;

public class OrgBO {

    private String orgid;
    @JSONField(ordinal = 1)
    private String orgname;
    @JSONField(ordinal = 2)
    private List<CustomizationInterfaceBean> interfaces;

    public String getOrgid() {
        return orgid;
    }

    public void setOrgid(String orgid) {
        this.orgid = orgid;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public List<CustomizationInterfaceBean> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<CustomizationInterfaceBean> interfaces) {
        this.interfaces = interfaces;
    }
}
