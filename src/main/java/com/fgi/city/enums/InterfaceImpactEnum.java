package com.fgi.city.enums;

/**
 * 以下接口参数修改对接口封装没有影响
 */
public enum InterfaceImpactEnum {
    // 接口名称、接口排序号、接口描述
    INTERFACENAME, SORTNUM, INTERFACEDESC, UPDATETIME;

    public static boolean containsKey(String key) {
        for (InterfaceImpactEnum c : InterfaceImpactEnum.values()) {
            if (c.name().equals(key)) {
                return true;
            }
        }
        return false;
    }
}
