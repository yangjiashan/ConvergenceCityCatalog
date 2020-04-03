package com.fgi.city.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.SecretKeyMapper;
import com.fgi.city.entity.SecretKeyBean;
import com.fgi.city.entity.UserBean;
import com.fgi.city.enums.FailReasonEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.service.EncryptionService;
import com.fgi.city.utils.NETUserSignonUtil;
import com.fgi.city.utils.SM2Util;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncryptionServiceImpl implements EncryptionService {
    private transient Logger logger = LoggerFactory.getLogger(EncryptionServiceImpl.class);

    @Autowired
    private HttpRequestService httpRequestService;

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private NETUserSignonUtil netUserSignonUtil;

    @Autowired
    private SecretKeyMapper secretKeyMapper;

    /**
     * 获取凭证
     *
     * @param jsonData
     * @param result
     */
    @Override
    @HystrixCommand(groupKey = "GUIDGroup", commandKey = "getGUID", fallbackMethod = "fallBack_Encrypt")
    public void getGUID(JSONObject jsonData, JSONObject result) {
        try {
            String username = jsonData.getString("username");
            String password = jsonData.getString("password");
            logger.info("收到的username:" + username);
            logger.info("收到的password:" + password);
            if (!httpRequestService.checkNotEmpty(jsonData, httpRequestService.needParams("username", "password"), result))
                return;
            String d_name = SM2Util.getInstance().SM2Decrypt(username, configBean.getHjsm2prikey());
            logger.info("收到的username解密:" + d_name);
            if (d_name == null) {
                // 账户名解密错误，请用正确的汇聚SM2公钥加密
                result.put("code", ResultStatusEnum.FAILURE07.getCode());
                result.put("message", FailReasonEnum.FAIL_04.getDesc());
                return;
            }
            String d_pwd = SM2Util.getInstance().SM2Decrypt(password, configBean.getHjsm2prikey());
            logger.info("收到的password解密:" + d_pwd);
            if (d_pwd == null) {
                // 密码解密错误，请用正确的汇聚SM2公钥加密
                result.put("code", ResultStatusEnum.FAILURE07.getCode());
                result.put("message", FailReasonEnum.FAIL_05.getDesc());
                return;
            }
            try {
//            String guid = GuidUtil.getInstance().getGuid(d_name, d_pwd, configBean.getGuidurl());
                String guid = netUserSignonUtil.getGUid(d_name, d_pwd);
                if (StringUtils.isBlank(guid)) {
                    // 账号密码错误
                    result.put("code", ResultStatusEnum.FAILURE02.getCode());
                    result.put("message", FailReasonEnum.FAIL_06.getDesc());
                } else {
                    // 加密guid
                    encryptMsg(result, guid);
                }
            } catch (Exception e) {
                // 获取guid异常
                result.put("message", FailReasonEnum.FAIL_09.getDesc());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }

    }

    /**
     * 获取SM4秘钥
     *
     * @param jsonData
     * @param result
     */
    @Override
    @HystrixCommand(groupKey = "SecretKeyGroup", commandKey = "getSecretKey", fallbackMethod = "fallBack_Encrypt")
    public void getSecretKey(JSONObject jsonData, JSONObject result) {
        try {
            String guid = jsonData.getString("guid");
            if (!httpRequestService.checkNotEmpty(jsonData, httpRequestService.needParams("guid"), result))
                return;
            String d_guid = SM2Util.getInstance().SM2Decrypt(guid, configBean.getHjsm2prikey());
            if (d_guid == null) {
                // guid解密失败
                result.put("code", ResultStatusEnum.FAILURE07.getCode());
                result.put("message", FailReasonEnum.FAIL_13.getDesc());
                return;
            }
            // 判断guid是否是有效的
            if (!netUserSignonUtil.IsValidguid(d_guid)) {
                result.put("code", ResultStatusEnum.FAILURE03.getCode());
                result.put("message", FailReasonEnum.FAIL_14.getDesc());
                return;
            }
            // 获取该机构的sm4秘钥
            UserBean users = netUserSignonUtil.getUserInfo(d_guid);
            SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(users.getOrganizationID());
            // 获取该机构SM2公钥
            SecretKeyBean SM2KeyBean = secretKeyMapper.querySM2KeyByOrgId(users.getOrganizationID());
            if (SM2KeyBean == null || StringUtils.isBlank(SM2KeyBean.getSecretkey())) {
                // 该用户所属的机构还未与省平台交换公钥
                result.put("message", FailReasonEnum.FAIL_28.getDesc());
                return;
            }
            if (secretKeyBean == null || StringUtils.isBlank(secretKeyBean.getSecretkey())) {
                // 该用户所属的机构还未配置SM4秘钥
                result.put("code", ResultStatusEnum.FAILURE05.getCode());
                result.put("message", FailReasonEnum.FAIL_15.getDesc());
                return;
            }
            // 使用调用者公钥加密数据
            String encryptSM4 = SM2Util.getInstance().SM2Encrypt(secretKeyBean.getSecretkey(), SM2KeyBean.getSecretkey());
            if (encryptSM4 != null) {
                result.put("code", ResultStatusEnum.SUCCUSS.getCode());
                result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
                result.put("sm4key", encryptSM4);
            } else {
                result.put("message", FailReasonEnum.FAIL_11.getDesc());
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 公钥加密
     *
     * @param result
     * @param guid
     */
    private void encryptMsg(JSONObject result, String guid) {
        try {
            // 获取该机构的sm4秘钥
            UserBean users = netUserSignonUtil.getUserInfo(guid);
            // 获取机构SM2公钥
            SecretKeyBean SM2KeyBean = secretKeyMapper.querySM2KeyByOrgId(users.getOrganizationID());
            if (SM2KeyBean == null || StringUtils.isBlank(SM2KeyBean.getSecretkey())) {
                // 该用户所属的机构还未和省平台交换SM2公钥
                result.put("message", FailReasonEnum.FAIL_28.getDesc());
            } else {
                String encryptGuid = SM2Util.getInstance().SM2Encrypt(guid, SM2KeyBean.getSecretkey());
                if (encryptGuid != null) {
                    result.put("code", ResultStatusEnum.SUCCUSS.getCode());
                    result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
                    result.put("guid", encryptGuid);
                } else {
                    result.put("message", FailReasonEnum.FAIL_11.getDesc());
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 降级方法
     *
     * @param jsonData
     * @param result
     * @return
     */
    private void fallBack_Encrypt(JSONObject jsonData, JSONObject result) {
        result.put("code", ResultStatusEnum.FAILURE.getCode());
        result.put("message", FailReasonEnum.FAIL_17.getDesc());
        logger.info(result.toJSONString());
    }
}
