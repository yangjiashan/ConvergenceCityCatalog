package com.fgi.city.enums;

/**
 * 参数类型
 */
public enum FieldTypeEnum {
    OTHER_TYPE("00", "其他"),
    ACCOUNT_TYPE("01", "账号类型"),
    PASSWORD_TYPE("02", "密码类型"),
    TOKEN_TYPE("03", "口令类型"),
    ;

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

    private String val;
    private String desc;

    private FieldTypeEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }
}
