package com.fgi.city.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.utils.NETUserSignonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 手动对地市tocken接口封装（接口自动封装使用）
 * 方法名称使用 method_对应方地区码，如：method_01
 * 参数暂定为账号密码，返回为json字符串格式如：{guid:'xxxx',message:'xxxx'}
 * 调用失败返回guid为空字符串，message为错误原因，格式如：{guid:'',message:'账号密码不存在'}
 */
@Component
public class PermissionInterfaceService {
    @Autowired
    NETUserSignonUtil netUserSignonUtil;

//    宁德市	03
//    莆田市	04
//    福州市	01
//    厦门市	02
//    泉州市	05
//    漳州市	06
//    龙岩市	07
//    三明市	08
//    南平市	09
//    平潭综合实验区 10

    public String method_00(String username, String password) {
        JSONObject result = new JSONObject();
        result.put("guid","sddddddddddbvgflsdjflj");
        result.put("message", "调用成功");
        return result.toJSONString();
    }

    // 先以空间中心guid获取方式为例
    public String method_02(String username, String password) {
        String guid = netUserSignonUtil.getGUid(username, password);
        JSONObject result = new JSONObject();
        result.put("guid",guid);
        result.put("message", StringUtils.isBlank(guid)?"调用失败":"调用成功");
        return result.toJSONString();
    }
}
