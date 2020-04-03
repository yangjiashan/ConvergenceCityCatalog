package com.fgi.city.enums;

public enum FailReasonEnum {

    FAIL_00("00","内部异常"),
    FAIL_01("01","输入参数有误"),
    FAIL_02("02","请指定操作类型，addorupdate、delete"),

    FAIL_03("03","账号、密码不能为空"),
    FAIL_04("04","账户名解密错误，请用正确的汇聚SM2公钥加密"),
    FAIL_05("05","密码解密错误，请用正确的汇聚SM2公钥加密"),
    FAIL_06("06","账号或密码错误"),
    FAIL_07("07","读取参数异常，请传入正确参数"),
    FAIL_08("08","转换json异常，请传入正确的json格式参数"),
    FAIL_09("09","获取guid异常，请检查网络"),
    FAIL_10("10","调用者sm2公钥不能为空"),
    FAIL_11("11","返回时加密失败，请上传正确的调用者SM2公钥"),


    FAIL_12("12","guid不能为空"),
    FAIL_13("13","guid解密失败"),
    FAIL_14("14","guid不正确或已经失效"),
    FAIL_15("15","该用户所属的机构还未配置SM4秘钥"),
    FAIL_16("16","调用者公钥解密失败"),

    FAIL_17("17","请稍后重试"),
    FAIL_18("18","请填写必传参数"),
    FAIL_19("19","data解密失败"),
    FAIL_20("20","data数据格式不正确转换json失败"),

    FAIL_21("21","操作失败，机构不存在"),
    FAIL_22("22","地区码未找到，请先上报机构"),
    FAIL_23("23","参数值错误"),
    FAIL_24("24","请先上报该地区的公共业务事项"),
    FAIL_25("25","请先上报指标项"),
    FAIL_26("26","请先上报对应接口"),
    FAIL_27("27","删除失败，请先公有的业务信息再删除公共业务事项"),
    FAIL_28("28","该用户所属的机构还未和省平台交换SM2公钥"),
    FAIL_29("29","验签失败"),
    FAIL_30("30","返回时签名失败"),
    FAIL_31("31","methodid值不存在"),

    FAIL_32("32","接口名称不能为空"),
    FAIL_33("33","接口名称、方法名称对应不到口令接口"),
    FAIL_34("34","接口操作日志查询日期不能超过7天"),
    FAIL_35("35","日期格式解析失败，请传入正确格式的日期"),

    FAIL_36("36","业务系统不存在"),
    FAIL_37("37","业务事项不存在"),
    FAIL_38("38","业务信息不存在"),
    FAIL_39("39","接口不存在"),
    FAIL_40("40","接口方法不存在"),
    FAIL_41("41","interfaceid值不存在"),
    FAIL_42("42","请输入正确的地市编码"),
    FAIL_43("43","该机构不存在！"),
    FAIL_44("44","该指标不存在！"),
    FAIL_45("45","有口令接口时，机构不能为空！"),
    ;

    private String code;
    private String desc;

    private FailReasonEnum(String code, String desc) {
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
