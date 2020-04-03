package com.fgi.city.enums;

/**
 * 方法形参参数修改为封装没有影响
 */
public enum FieldImpactEnum {
    // 参数描述
    DESCRIBE, CREATETIME, UPDATETIME;

    public static boolean containsKey(String key) {
        for (FieldImpactEnum c : FieldImpactEnum.values()) {
            if (c.name().equals(key)) {
                return true;
            }
        }
        return false;
    }
}
