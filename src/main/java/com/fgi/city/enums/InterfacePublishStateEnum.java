package com.fgi.city.enums;

/**
 * 接口发布状态
 */
public enum InterfacePublishStateEnum {

    // 接口发布状态，0->未发布   1->待测试    2->已测试   3->已更新未处理  4->已删除未处理  5->已删除  6->禁用  7->启用
    NOPUBLISH("0", "未发布"),
    WAITINGTEST("1", "待测试"),
    TESTED("2", "已测试"),
    UPDATENODEAL("3", "已更新未处理"),
    DELETENODEAL("4", "已删除未处理"),
    DELETED("5", "已删除"),
    DISABLE("6", "禁用"),
    ENABLE("7", "启用"),
    ;

    private String code;
    private String desc;

    InterfacePublishStateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
