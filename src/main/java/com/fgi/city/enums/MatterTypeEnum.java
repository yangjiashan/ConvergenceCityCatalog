package com.fgi.city.enums;

public enum MatterTypeEnum {
    PUB_MATTER("2", "公有事项"),
    PRI_MATTER("1", "私有事项"),
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

    private MatterTypeEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }
}
