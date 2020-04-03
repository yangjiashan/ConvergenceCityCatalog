package com.fgi.city.entity;

/**
 * 接口变更信息类
 */
public class InterfaceChangeBean {

    private String id; // 主键
    private String changetableid; // 关联地市接口表、地市接口方法表、地市接口参数表的id
    private String changetable; // 变更表：地市接口、地市接口方法、地市接口参数
    private String changecolumn; // 变更字段
    private String oldvalue; // 原值
    private String newvalue; // 新值
    private String type; // 变更类型，0：新增、1：删除、2：修改
    private String createtime; // 创建时间
    private String creatorid; // 创建人id
    private String flag; // 是否删除数据，0：有效，1：无效
    private String batch; // 批次号 （用 生成id的随机串生成 ）

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChangetableid() {
        return changetableid;
    }

    public void setChangetableid(String changetableid) {
        this.changetableid = changetableid;
    }

    public String getChangetable() {
        return changetable;
    }

    public void setChangetable(String changetable) {
        this.changetable = changetable;
    }

    public String getChangecolumn() {
        return changecolumn;
    }

    public void setChangecolumn(String changecolumn) {
        this.changecolumn = changecolumn;
    }

    public String getOldvalue() {
        return oldvalue;
    }

    public void setOldvalue(String oldvalue) {
        this.oldvalue = oldvalue;
    }

    public String getNewvalue() {
        return newvalue;
    }

    public void setNewvalue(String newvalue) {
        this.newvalue = newvalue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getCreatorid() {
        return creatorid;
    }

    public void setCreatorid(String creatorid) {
        this.creatorid = creatorid;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
