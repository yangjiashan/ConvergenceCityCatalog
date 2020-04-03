package com.fgi.city.bo;

/**
 * 方法参数信息
 */
public class MethodFieldBO {

    private String id; // 主键
    private String methodid; // 方法id
    private String fieldtype; // 字段类型，0、输入，1、输出
    private String sortnum; // 排序号
    private String paramname; // 参数名称
    private String paramtype; // 参数类型，01:账号类型，02:密码类型，03:口令类型，00：其他
    private String describe; // 参数描述
    private String createtime; // 创建时间
    private String updatetime; // 更新时间
    private String paramvalue; // 参数示例值
    private String state; // 状态，1->正常数据，0->已删除数据 （逻辑删）

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

    public String getSortnum() {
        return sortnum;
    }

    public void setSortnum(String sortnum) {
        this.sortnum = sortnum;
    }

    public String getParamname() {
        return paramname;
    }

    public void setParamname(String paramname) {
        this.paramname = paramname;
    }

    public String getParamtype() {
        return paramtype;
    }

    public void setParamtype(String paramtype) {
        this.paramtype = paramtype;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
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

    public String getParamvalue() {
        return paramvalue;
    }

    public void setParamvalue(String paramvalue) {
        this.paramvalue = paramvalue;
    }
}
