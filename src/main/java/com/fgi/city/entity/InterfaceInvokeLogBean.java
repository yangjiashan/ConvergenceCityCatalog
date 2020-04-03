package com.fgi.city.entity;

/**
 * 接口调用日志
 */
public class InterfaceInvokeLogBean {

    private String id; // 主键
    private String interfaceid; // 接口ID interface_city 主键
    private String methodid; // 方法ID interface_method_city 主键
    private String invoke_org; // 调用者机构
    private String invoke_account; // 调用者账号
    private String invoke_name; // 调用者姓名
    private String interface_name; // 接口名称
    private String method_cname; // 方法中文名称
    private String invoke_time; // 调用时间
    private String createtime; // 创建时间
    private String method_ename; // 方法英文名称
    private String interface_type; // 接口类型，传字典，对应dictionary_city中typename=字典_接口类型
    private String platform; // 访问平台
    private String platformid; // 访问平台字典id


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

    public String getInvoke_org() {
        return invoke_org;
    }

    public void setInvoke_org(String invoke_org) {
        this.invoke_org = invoke_org;
    }

    public String getInvoke_account() {
        return invoke_account;
    }

    public void setInvoke_account(String invoke_account) {
        this.invoke_account = invoke_account;
    }

    public String getInvoke_name() {
        return invoke_name;
    }

    public void setInvoke_name(String invoke_name) {
        this.invoke_name = invoke_name;
    }

    public String getInterface_name() {
        return interface_name;
    }

    public void setInterface_name(String interface_name) {
        this.interface_name = interface_name;
    }

    public String getMethod_cname() {
        return method_cname;
    }

    public void setMethod_cname(String method_cname) {
        this.method_cname = method_cname;
    }

    public String getInvoke_time() {
        return invoke_time;
    }

    public void setInvoke_time(String invoke_time) {
        this.invoke_time = invoke_time;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getMethod_ename() {
        return method_ename;
    }

    public void setMethod_ename(String method_ename) {
        this.method_ename = method_ename;
    }

    public String getInterface_type() {
        return interface_type;
    }

    public void setInterface_type(String interface_type) {
        this.interface_type = interface_type;
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
}
