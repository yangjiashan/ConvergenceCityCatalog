package com.fgi.city.entity;

/**
 * 地市接口权限配置
 */
public class InterfacePermissionBean {

    private String id; // 主键
    private String interfaceid; // 接口id
    private String methodid; // 方法id
    private String orgid; // 访问机构id
    private String account; // 访问单点账号
    private String displayname; // 访问姓名
    private String orgname; // 访问机构名称
    private String platform; // 访问平台
    private String platformid; // 访问平台字典id
    private String callnumber; // 访问次数
    private String visit; // 当天当前调用次数;

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceid() {
        return interfaceid;
    }

    public void setInterfaceid(String interfaceid) {
        this.interfaceid = interfaceid;
    }

    public String getMethodid() {
        return methodid;
    }

    public void setMethodid(String methodid) {
        this.methodid = methodid;
    }

    public String getOrgid() {
        return orgid;
    }

    public void setOrgid(String orgid) {
        this.orgid = orgid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformid() {
        return platformid;
    }

    public void setPlatformid(String platformid) {
        this.platformid = platformid;
    }

    public String getCallnumber() {
        return callnumber;
    }

    public void setCallnumber(String callnumber) {
        this.callnumber = callnumber;
    }
}
