package com.fgi.city.enums;

/**
 * 方法请求参数格式
 */
public enum ParamFormatEnum {

    JSON_FORMAT("01", "json格式参数（body中附加json格式字符串）"),
    KV_FORMAT("02", "键值对格式字符串"),
        ;

    private String val;
    private String desc;

    private ParamFormatEnum(String val, String desc) {
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
