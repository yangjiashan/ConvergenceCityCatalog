package com.fgi.city.bo;

import java.util.List;

public class InterfaceBO {
    private String id;// 主键
    private String orgid;// 机构ID,orgnization_city主键
    private String interfacename;// 接口名称
    private String interfacetype;// 接口类型，传字典，对应dictionary_city中typename=字典_接口类型，默认为webservice
    private String sortnum; // 用于显示排序
    private String createtime; // 创建时间
    private String updatetime; // 更新时间
    private String interfaceurl; // 接口地址
    private String interfacedesc; // 接口描述
    private String state; // 状态，1->正常数据，0->已删除数据 （逻辑删）
    List<MethodInfoBO> methodinfos;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgid() {
        return orgid;
    }

    public void setOrgid(String orgid) {
        this.orgid = orgid;
    }

    public String getInterfacename() {
        return interfacename;
    }

    public void setInterfacename(String interfacename) {
        this.interfacename = interfacename;
    }

    public String getInterfacetype() {
        return interfacetype;
    }

    public void setInterfacetype(String interfacetype) {
        this.interfacetype = interfacetype;
    }

    public String getSortnum() {
        return sortnum;
    }

    public void setSortnum(String sortnum) {
        this.sortnum = sortnum;
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

    public String getInterfaceurl() {
        return interfaceurl;
    }

    public void setInterfaceurl(String interfaceurl) {
        this.interfaceurl = interfaceurl;
    }

    public String getInterfacedesc() {
        return interfacedesc;
    }

    public void setInterfacedesc(String interfacedesc) {
        this.interfacedesc = interfacedesc;
    }

    public List<MethodInfoBO> getMethodinfos() {
        return methodinfos;
    }

    public void setMethodinfos(List<MethodInfoBO> methodinfos) {
        this.methodinfos = methodinfos;
    }
}
