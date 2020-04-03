package com.fgi.city.enums;

/**
 * 操作类型枚举类
 */
public enum OperatorEnum {
    ADDORUPDATE("addOrUpdate", "增加或更新"), DELETE("delete", "删除");

    private String val;
    private String desc;

    private OperatorEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
