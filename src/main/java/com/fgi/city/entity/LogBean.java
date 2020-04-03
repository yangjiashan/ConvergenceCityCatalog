package com.fgi.city.entity;

public class LogBean {

    private String id;
    private String username; // 用户名
    private String orgname; // 用户所在机构名称
    private String singleorgid; // 用户所在机构单点机构ID
    private String interfacename; // 访问接口名称
    private String accesstype; // 访问类型，addOrUpdate, delete
    private String inputparams; // 输入参数
    private String outputparams; // 输出参数
    private String status; // 状态
    private String createtime; // 创建时间

    public LogBean() {
    }

    public LogBean(String username, String orgname, String singleorgid, String interfacename, String accesstype, String inputparams, String outputparams, String status) {
        this.username = username;
        this.orgname = orgname;
        this.singleorgid = singleorgid;
        this.interfacename = interfacename;
        this.accesstype = accesstype;
        this.inputparams = inputparams;
        this.outputparams = outputparams;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public String getSingleorgid() {
        return singleorgid;
    }

    public void setSingleorgid(String singleorgid) {
        this.singleorgid = singleorgid;
    }

    public String getInterfacename() {
        return interfacename;
    }

    public void setInterfacename(String interfacename) {
        this.interfacename = interfacename;
    }

    public String getAccesstype() {
        return accesstype;
    }

    public void setAccesstype(String accesstype) {
        this.accesstype = accesstype;
    }

    public String getInputparams() {
        return inputparams;
    }

    public void setInputparams(String inputparams) {
        this.inputparams = inputparams;
    }

    public String getOutputparams() {
        return outputparams;
    }

    public void setOutputparams(String outputparams) {
        this.outputparams = outputparams;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }
}
