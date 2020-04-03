package com.fgi.city.Test;

import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.entity.AttributeBean;
import com.fgi.city.enums.PermissionsTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http接口模板
 */
@RestController(value = "mapper_class_name")
public class Templenate {

    // 获取输入、输出参数
    @Autowired
    private InterfaceMapper interfaceMapper;

    private String methodId = "32564SGFS3245..."; // 动态发布那个接口，地市上报后存在数据库的方法ID

    private String account = "hj_123456"; // 地市交换给省平台的账号

    private String password = "123456";// 地市交换给省平台的密码


    /**
     * 地市的接口参数形式是 get方式
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/mapper_method_name")
    public String method_hj_name(HttpServletRequest request) {
        // 查询该地市接口输入参数
        List<AttributeBean> attributeBeans = interfaceMapper.queryInterfaceAttribute(methodId, "0");
        Map<String, String> map = new HashMap<>();
        // 判断类型（账号密码类型、口令类型、其他类型）
        int res = judgeParams(attributeBeans, map);
        // 存储账号、密码、口令参数
        Map<String, String> dealMap = getParams(map, res);
        // 替换我们自己的账号、密码，口令，得到处理后的参数集合
        Map<String, String> replacedParams = replaceParams(request.getParameterNames(), request, dealMap, res);
        // 添加省平台日志
        return sendRquest(methodId, replacedParams);
    }

    /**
     * 存储账号、密码、口令参数
     *
     * @param map
     * @param res
     * @return
     */
    private Map<String, String> getParams(Map<String, String> map, int res) {
        String account_param = "";
        String password_param = "";
        String token_param = "";
        if (res == 0) {
            // 账号、密码类型
            account_param = map.get("account");
            password_param = map.get("password");
        } else if (res == 1) {
            // token类型
            token_param = map.get("token");
        } else {
            // 不需要授权
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(account_param, "account");
        paramMap.put(password_param, "password");
        paramMap.put(token_param, "token");
        return paramMap;
    }

    /**
     * 替换参数
     *
     * @param parameterMap
     * @param request
     * @param map
     * @param type
     * @return
     */
    private Map<String, String> replaceParams(Enumeration<String> parameterMap, HttpServletRequest request, Map<String, String> map, int type) {
        String guid = "";
        if (type == 1) {
            guid = "12345768900hghfg";
        }
        Map<String, String> result = new HashMap<>();
        while (parameterMap.hasMoreElements()) {
            String params = parameterMap.nextElement();
            String values = request.getParameter(params);
            if (map.containsKey(params)) {
                if (type == 0) {
                    if ("account".equals(map.get(params))) {
                        // 账号类型
                        values = account;
                    } else if ("password".equals(map.get(params))) {
                        // 密码类型
                        values = password;
                    }
                } else if (type == 1) {
                    // 口令类型
                    values = guid;
                }
            }
            result.put(params, values);
        }
        return result;
    }

    /**
     * 发送请求
     *
     * @param methodId
     * @param beans
     * @return
     */
    private String sendRquest(String methodId, Map<String, String> beans) {
        return "success";
    }

    /**
     * 判断入参是否是账号密码类型、token类型、其他类型
     *
     * @param attributeBeans
     * @return
     */
    private int judgeParams(List<AttributeBean> attributeBeans, Map<String, String> map) {
        int accountType = 0;
        int passwordType = 0;
        int tokenType = 0;
        for (AttributeBean attr : attributeBeans) {
            // 参数类型，01:账号类型，02:密码类型，03:口令类型，00：其他
            String attrType = attr.getParamtype();
            if (PermissionsTypeEnum.ACCOUNT_TYPE.getVal().equals(attrType)) {
                // 账号类型
                accountType = accountType + 1;
                map.put("account", attr.getParamname());
            } else if (PermissionsTypeEnum.PASSWORD_TYPE.getVal().equals(attrType)) {
                // 密码类型
                passwordType = passwordType + 1;
                map.put("password", attr.getParamname());
            } else if (PermissionsTypeEnum.TOKEN_TYPE.getVal().equals(attrType)) {
                // 口令类型
                tokenType = tokenType + 1;
                map.put("token", attr.getParamname());
            }
        }
        if (accountType != 0 && passwordType != 0) {
            // 确定是账号、密码类型
            return 0;
        }
        if (tokenType != 0) {
            // token类型
            return 1;
        }
        return -1;
    }


    public InterfaceMapper getInterfaceMapper() {
        return interfaceMapper;
    }

    public void setInterfaceMapper(InterfaceMapper interfaceMapper) {
        this.interfaceMapper = interfaceMapper;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }
}
