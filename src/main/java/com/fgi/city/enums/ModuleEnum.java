package com.fgi.city.enums;

public enum ModuleEnum {
    ORG_REPORT("ORG_REPORT", "机构上报"),
    SYS_REPORT("SYS_REPORT", "业务系统上报"),
    CATALOG_REPORT("CATALOG_REPORT", "业务事项上报"),
    PUBMATTER_REPORT("PUBMATTER_REPORT", "公共业务事项上报"),
    ;

    private String val;
    private String desc;

    private ModuleEnum(String val, String desc) {
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
