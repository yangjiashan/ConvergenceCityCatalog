package com.fgi.city.entity;

/**
 * 数据预览接口信息
 */
public class DataPreviewInfo {

    private String id; // 主键
    private String areacode; // 区市字典编码，从设区市字典表获得
    private String dataurl; // 数据预览接口地址
    private String reportorg; // 上报者机构id(单点)
    private String reportaccount; // 上报者账号
    private String createtime; // 创建时间
    private String updatetime; // 更新时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public String getDataurl() {
        return dataurl;
    }

    public void setDataurl(String dataurl) {
        this.dataurl = dataurl;
    }

    public String getReportorg() {
        return reportorg;
    }

    public void setReportorg(String reportorg) {
        this.reportorg = reportorg;
    }

    public String getReportaccount() {
        return reportaccount;
    }

    public void setReportaccount(String reportaccount) {
        this.reportaccount = reportaccount;
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
}
