package com.fgi.city.enums;

public enum PermissionsTypeEnum {

    ACCOUNT_TYPE("01", "账号类型"),
    PASSWORD_TYPE("02", "密码类型"),
    TOKEN_TYPE("03", "口令类型"),
    OTHER_TYPE("00", "其他"),
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

    private PermissionsTypeEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }
}
