package com.fgi.city.service.impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.SecretKeyMapper;
import com.fgi.city.entity.SecretKeyBean;
import com.fgi.city.entity.UserBean;
import com.fgi.city.enums.FailReasonEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.utils.NETUserSignonUtil;
import com.fgi.city.utils.SM2Util;
import com.fgi.city.utils.Sm4Util;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求相关服务类
 */
@Service
public class HttpRequestService {
    private transient Logger logger = LogManager.getLogger(HttpRequestService.class);
    @Autowired
    private NETUserSignonUtil netUserSignonUtil;
    @Autowired
    private ConfigBean configBean;
    @Autowired
    private SecretKeyMapper secretKeyMapper;

    /**
     * 获取http request中post参数
     *
     * @param request
     * @return
     * @throws Exception
     */
    public JSONObject getRequestJsonData(HttpServletRequest request, JSONObject result) {
        result.put("code", ResultStatusEnum.FAILURE.getCode());
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            if (StringUtils.isBlank(responseStrBuilder.toString())) {
                result.put("message", FailReasonEnum.FAIL_07.getDesc());
            }
            return JSONObject.parseObject(responseStrBuilder.toString());
        } catch (IOException e) {
            // 读取参数异常，请传入正确参数
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_07.getDesc());
        } catch (JSONException je) {
            // 转换json异常，请传入正确的json格式参数
            logger.error(ExceptionUtils.getStackTrace(je));
            result.put("message", FailReasonEnum.FAIL_08.getDesc());
        }
        return null;
    }

    public static void main(String[] args) {
        String tempStr = "{\"infotype\":\"1,6\",\"query_businessmatter_cname\":\"德化不动产登记\",\"orgname\":\"德化县自然资源局\",\"businessmatter_ename\":\"table39639\",\"opetype\":\"addOrUpdate\",\"sysname\":\"福建省不动产登记信息系统（德化）\",\"catalogname\":\"德化不动产登记\",\"businessmatter_cname\":\"德化不动产登记\",\"sortnum\":384}";
        JSONObject.parseObject(tempStr);



    }

    // 检查必传参数(值不可是空字符串)
    public boolean checkNotEmpty(JSONObject jsonObject, List<String> list, JSONObject result) {
        for (String str : list) {
            if (jsonObject.containsKey(str)) {
                if (jsonObject.getString(str) == null || StringUtils.isBlank(jsonObject.getString(str))) {
                    // 请填写必传的参数
                    result.put("code", ResultStatusEnum.FAILURE06.getCode());
                    result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + str);
                    return false;
                }
            } else {
                // 请填写必传的参数
                result.put("code", ResultStatusEnum.FAILURE06.getCode());
                result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + str);
                return false;
            }
        }
        return true;
    }

    // 检查必传参数(值可是空字符串)
    public boolean checkNeedParam(JSONObject jsonObject, List<String> list, JSONObject result) {
        for (String str : list) {
            if (jsonObject.containsKey(str)) {
                if (jsonObject.getString(str) == null) {
                    // 请填写必传的参数
                    result.put("code", ResultStatusEnum.FAILURE06.getCode());
                    result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + str);
                    return false;
                }
            } else {
                // 请填写必传的参数
                result.put("code", ResultStatusEnum.FAILURE06.getCode());
                result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + str);
                return false;
            }
        }
        return true;
    }

    // 添加必传参数
    public List<String> needParams(String... str) {
        List<String> list = new ArrayList<>();
        for (String s : str) {
            list.add(s);
        }
        return list;
    }

    // 解密并检查guid有效性，有效则返回解密后的guid，无效返回空字符串
    public String checkGuid(JSONObject jsonObject, JSONObject result) {
        // 检查guid不为空
        if (!checkNotEmpty(jsonObject, needParams("guid"), result))
            return "";
        // guid解密
        logger.info("收到的guid是："+jsonObject.getString("guid"));
        String d_guid = SM2Util.getInstance().SM2Decrypt(jsonObject.getString("guid"), configBean.getHjsm2prikey());
        if (d_guid == null) {
            // guid解密失败
            result.put("code", ResultStatusEnum.FAILURE07.getCode());
            result.put("message", FailReasonEnum.FAIL_13.getDesc());
            return "";
        }
        // 判断guid是否是有效的
        if (!netUserSignonUtil.IsValidguid(d_guid)) {
            result.put("code", ResultStatusEnum.FAILURE03.getCode());
            result.put("message", FailReasonEnum.FAIL_14.getDesc());
            return "";
        }
        return d_guid;
    }

    // 获取data
    public JSONObject getData(JSONObject jsonObject, JSONObject result, String guid) {
        if (!checkNotEmpty(jsonObject, needParams("data", "sign"), result))
            return null;
        // 验签
        String sign = jsonObject.getString("sign");
        String data = jsonObject.getString("data");
        // 根据guid获取机构id
        UserBean users = netUserSignonUtil.getUserInfo(guid);
        // 获取该单位的SM2公钥
        SecretKeyBean SM2KeyBean = secretKeyMapper.querySM2KeyByOrgId(users.getOrganizationID());
        if (SM2KeyBean == null || StringUtils.isBlank(SM2KeyBean.getSecretkey())) {
            // 该用户所属的机构还未和省平台交换SM2公钥
            result.put("message", FailReasonEnum.FAIL_28.getDesc());
            return null;
        }
        boolean signResult = false;
        try {
            // 验签
            signResult = SM2Util.getInstance().SM2Verify(data, SM2KeyBean.getSecretkey(), sign);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
        }
        if (!signResult) {
            result.put("message", FailReasonEnum.FAIL_29.getDesc());
            return null;
        }
        // 获取该单位的SM4秘钥
        SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(users.getOrganizationID());
        if (secretKeyBean == null || StringUtils.isBlank(secretKeyBean.getSecretkey())) {
            // 该用户所属的机构还未配置SM4秘钥
            result.put("message", FailReasonEnum.FAIL_15.getDesc());
            return null;
        }
        String d_data = Sm4Util.getInstance().SM4Decrypt(data, "SM4/ECB/PKCS5Padding", secretKeyBean.getSecretkey(), null);
        if (d_data == null) {
            // 解密失败
            result.put("message", FailReasonEnum.FAIL_19.getDesc());
            return null;
        }
        // 转换成json对象
        try {
            JSONObject res = JSONObject.parseObject(d_data);
            res.put(configBean.getParsed_guid(), guid);
            res.put(configBean.getParsed_sgid(), users.getOrganizationID());
            res.put(configBean.getParsed_user(), users);
            jsonObject.put(configBean.getParsed_data(), d_data);
            jsonObject.put(configBean.getParsed_user(), users);
            return res;
        } catch (JSONException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_20.getDesc());
            return null;
        }
    }
}
