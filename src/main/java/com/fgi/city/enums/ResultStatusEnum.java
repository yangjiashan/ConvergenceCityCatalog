package com.fgi.city.enums;

/**
 * 返回状态枚举类
 */
public enum ResultStatusEnum {
    // 公共约定
    SUCCUSS("01", "操作成功"),
    FAILURE("-1", "操作失败"),
    FAILURE02("02", "账号不存在"),
    FAILURE03("03", "身份验证不通过或GUID过期"),
    FAILURE04("04", "获取sm2key（汇聚平台公钥）失败"),
    FAILURE05("05", "获取sm4密钥失败"),
    FAILURE06("06", "必填项缺失"),
    FAILURE07("07", "加解密失败"),

    // 目录通约定
    FAILURE100("100", "内部异常"),
    FAILURE101("101", "机构字段无法关联"),
    FAILURE102("102", "业务系统字段无法关联"),
    FAILURE103("103", "数据库字段无法关联"),
    FAILURE104("104", "业务数据表字段无法关联"),
    FAILURE105("105", "业务指标与数据无法匹配，请重新上传预览数据"),
    FAILURE106("106", "图标大小不能超过1M"),
    FAILURE107("107", "图标宽高请设置60*60"),
    FAILURE108("108", "图标文件格式请使用png格式"),
    FAILURE109("109", "图标文件解析错误"),
    FAILURE110("110", "还不存在上报者所属的机构，请先上报机构"),
    FAILURE111("111", "上报者所处的地市与要修改的地市不一致，修改失败"),
    FAILURE112("112", "添加失败，该机构已经存在！"),
    FAILURE113("113", "添加失败，该系统名称已经存在！"),
    FAILURE114("114", "添加失败，该业务数据库名称已经存在！"),
    FAILURE115("115", "添加失败，该业务数据表名称已经存在！"),
    FAILURE116("116", "添加失败，该业务指标名称已经存在！"),
    FAILURE117("117", "添加失败，使用的账号与上报的地区码不属于同一个地市！"),


    // 接口通约定
    FAILURE200("200", "接口无法关联"),
    FAILURE201("201", "添加失败，该接口名称已经存在！"),
    FAILURE202("202", "添加失败，该接口方法名称已经存在！"),

    // 接口封装
    FAILURE300("300", "内部异常"),
    FAILURE301("301", "guid不能为空"),
    FAILURE302("302", "guid无效"),
    FAILURE303("303", "该账号没有权限"),
    FAILURE304("304", "该账号当日访问已达到上限"),
    FAILURE305("305", "该接口还未开启使用"),
    FAILURE306("306", "参数读取失败，请传入正确格式参数"),
    FAILURE307("307", "该接口被禁用"),
    FAILURE308("308", "该接口还未封装"),
    FAILURE309("309", "该接口已经更新，等待重新封装"),
    FAILURE310("310", "接口不存在"),
    FAILURE311("311", "该接口已被删除"),
    ;

    private String code;
    private String desc;

    private ResultStatusEnum(String code, String desc) {
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
