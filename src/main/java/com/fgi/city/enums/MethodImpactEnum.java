package com.fgi.city.enums;

/**
 * 方法参数修改对接口封装没有影响，不需要重新封装
 */
public enum MethodImpactEnum {
    // 方法简称、方法描述、查询表、返回值示例
    METHODABBREVIATION, METHODDESCRIBE, QUERYTABLE, RESULTEXAMPLE, UPDATETIME, MONITORSTATUS, MONITORDESC, SORTNUM;

    public static boolean containsKey(String key) {
        for (MethodImpactEnum c : MethodImpactEnum.values()) {
            if (c.name().equals(key)) {
                return true;
            }
        }
        return false;
    }
}
