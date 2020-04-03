package com.fgi.city.enums;

/**
 * 接口变更需要修改的表
 */
public enum InterfaceChangeTableEnum {

    INTERFACE_TABLE("地市接口"),
    INTERFACE_METHOD_TABLE("地市接口方法"),
    INTERFACE_PARAM_TABLE("地市接口参数"),
    ;

    private String desc;

    private InterfaceChangeTableEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
