package com.fgi.city.template;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.bo.MethodFieldBO;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.entity.InterfaceInvokeLogBean;
import com.fgi.city.entity.InterfacePermissionBean;
import com.fgi.city.entity.UserBean;
import com.fgi.city.enums.FieldTypeEnum;
import com.fgi.city.enums.ParamFormatEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.service.impl.PermissionInterfaceService;
import com.fgi.city.utils.IdGenerateUtil;
import com.fgi.city.utils.NETUserSignonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.message.MessageContentsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class RequestTools {
    protected Logger logger = LoggerFactory.getLogger(RequestTools.class);
    @Autowired
    private NETUserSignonUtil netUserSignonUtil;
    @Autowired
    private PermissionInterfaceService permissionInterfaceService;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private InterfaceMapper interfaceMapper = BeanProvider.getBean(InterfaceMapper.class);

    // 添加接口调用日志
    public void addInvokeLog(InterfacePermissionBean permissionBean, Map<String, String> methodMap, String interfaceId, String methodId) {
        InterfaceInvokeLogBean logBean = new InterfaceInvokeLogBean();
        logBean.setId(IdGenerateUtil.getKey());
        logBean.setInterface_name(methodMap.get("INTERFACENAME")); // 接口名称
        logBean.setInterface_type(methodMap.get("INTERFACETYPE")); // 接口类型
        logBean.setInterfaceid(interfaceId);// 接口ID
        logBean.setMethod_cname(methodMap.get("METHOD_CNAME"));// 方法中文名称
        logBean.setMethod_ename(methodMap.get("METHODNAME"));// 方法英文名称
        logBean.setInvoke_account(permissionBean.getAccount()); // 调用账号
        logBean.setInvoke_name(permissionBean.getDisplayname()); // 调用姓名
        logBean.setInvoke_org(permissionBean.getOrgname()); // 调用机构
        logBean.setInvoke_time(format.format(new Date(System.currentTimeMillis()))); // 调用时间
        logBean.setMethodid(methodId);// 方法ID
        logBean.setPlatform(permissionBean.getPlatform());// 访问平台
        logBean.setPlatformid(permissionBean.getPlatformid());// 访问平台ID
        interfaceMapper.addInterfaceInvokeLog(logBean);
    }

    // 调用人工开发的token过去接口 返回json格式 {guid:'',message:''}
    public JSONObject invokePermission(String username, String password, String areacode) {
        try {
            logger.info("调用了一次地市口令获取接口， username:" + username + ", password:" + password + ", areacode:" + areacode);
            Class cls = Class.forName("com.fgi.city.service.impl.PermissionInterfaceService");
            Method method = cls.getDeclaredMethod("method_" + areacode, new Class[]{String.class, String.class});
//            Object object = method.invoke(cls.newInstance(), username, password);
            Object object = method.invoke(permissionInterfaceService, username, password);
            logger.info("返回guid为：" + String.valueOf(object));
            return JSONObject.parseObject(String.valueOf(object));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * FAILURE300("300", "内部异常"),
     * FAILURE301("301", "guid不能为空"),
     * FAILURE302("302", "guid无效"),
     * FAILURE303("303", "该账号没有权限"),
     * FAILURE304("304", "该账号当日访问已达到上限"),
     *
     * @param guid
     * @param methodId
     * @param result
     * @return
     */
    // 验证权限
    public boolean validatePermission(String guid, String methodId, JSONObject result, Map<String, InterfacePermissionBean> map) {
        // 验证guid是否有效
        if (StringUtils.isBlank(guid)) {
            // guid不能为空
            result.put("code", ResultStatusEnum.FAILURE301.getCode());
            result.put("message", ResultStatusEnum.FAILURE301.getDesc());
            return false;
        }
        if (!netUserSignonUtil.IsValidguid(guid)) {
            // guid无效
            result.put("code", ResultStatusEnum.FAILURE302.getCode());
            result.put("message", ResultStatusEnum.FAILURE302.getDesc());
            return false;
        }
        UserBean userBean = netUserSignonUtil.getUserInfo(guid);
        // 验证权限，根据methodid，account 查询权限
        InterfacePermissionBean permissionBean = new InterfacePermissionBean();
        permissionBean.setAccount(userBean.getAccount());
        permissionBean.setMethodid(methodId);
        // 验证每日访问次数，加锁
        synchronized (this) {
            InterfacePermissionBean interfacePermissionBean = interfaceMapper.queryInterfacePermissison(permissionBean);
            if (interfacePermissionBean == null) {
                // 该账号没有配置权限
                result.put("code", ResultStatusEnum.FAILURE303.getCode());
                result.put("message", ResultStatusEnum.FAILURE303.getDesc());
                return false;
            }
            String callNumberStr = interfacePermissionBean.getCallnumber();
            String visitStr = interfacePermissionBean.getVisit();
            int callNumber = -1;
            int visit = 0;
            try {
                callNumber = Integer.parseInt(callNumberStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                visit = Integer.parseInt(visitStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (callNumber == -1) {
                // 没有限制每天访问次数，访问次数+1
                visit = visit + 1;
            } else {
                if (callNumber <= 0) {
                    // 不能访问
                    result.put("code", ResultStatusEnum.FAILURE304.getCode());
                    result.put("message", ResultStatusEnum.FAILURE304.getDesc());
                    return false;
                } else {
                    // 可以访问，有限制每天访问次数
                    if (callNumber <= visit) {
                        // 达到访问上限
                        result.put("code", ResultStatusEnum.FAILURE304.getCode());
                        result.put("message", ResultStatusEnum.FAILURE304.getDesc());
                        return false;
                    } else {
                        // 访问次数+1
                        visit = visit + 1;
                    }
                }
            }
            interfacePermissionBean.setVisit(String.valueOf(visit));
            // 更新访问次数
            interfaceMapper.updateInterfaceVisit(interfacePermissionBean);
            map.put("permission", interfacePermissionBean);
        }
        return true;
    }

    /**
     * 设置请求参数，type=0表示webservice类型， type=1表示http类型
     *
     * @param methodId
     * @param messageContentsList
     * @param accountInfoMap
     * @param type
     * @param request
     * @param jsonData
     * @return
     */
    // 设置访问参数
    public JSONObject setParams(String methodId, MessageContentsList messageContentsList, Map<String, String> accountInfoMap, int type, HttpServletRequest request, JSONObject jsonData, String paramFormat) {
        // 根据方法ID获取输入参数（分两步）
        // (1) 先获取其他类型的参数列表
        JSONObject paramObject = new JSONObject();
        List<MethodFieldBO> otherFields = interfaceMapper.queryOhterFieldByMethodId(methodId);
        if (otherFields != null) {
            for (MethodFieldBO info : otherFields) {
                String originalValue = "";
                if (type == 0) {
                    // webservice
                    originalValue = String.valueOf(messageContentsList.get(otherFields.indexOf(info) + 1));
                } else if (type == 1) {
                    // 判断是参数格式
                    if (ParamFormatEnum.JSON_FORMAT.getVal().equals(paramFormat)) {
                        // json格式（附加在body中的json格式字符串）
                        originalValue = jsonData.getString(info.getParamname());
                    } else {
                        // 键值对，request中获取
                        originalValue = request.getParameter(info.getParamname());
                    }
                }
                paramObject.put(info.getParamname(), originalValue);
            }
        }
        // (2) 再获取token、账号、密码类型的数据
        List<MethodFieldBO> tokenFields = interfaceMapper.queryTokenFieldByMethodId(methodId);
        if (tokenFields != null) {
            for (MethodFieldBO info : tokenFields) {
                // 参数类型，01:账号类型，02:密码类型，03:口令类型
                String paramType = info.getParamtype();
                String originalValue = "";
                if (FieldTypeEnum.ACCOUNT_TYPE.getVal().equals(paramType)) {
                    // 如果是账号类型，就替换成省平台的账号
                    originalValue = accountInfoMap.get("USERNAME");
                } else if (FieldTypeEnum.PASSWORD_TYPE.getVal().equals(paramType)) {
                    // 如果是密码类型，就替换成省平台的密码
                    originalValue = accountInfoMap.get("PASSWORD");
                } else if (FieldTypeEnum.TOKEN_TYPE.getVal().equals(paramType)) {
                    // 如果是口令类型，根据账号密码获取地市口令
                    String areacode = accountInfoMap.get("AREACODE");
                    JSONObject jsonResult = invokePermission(accountInfoMap.get("USERNAME"), accountInfoMap.get("PASSWORD"), areacode);
                    if (jsonResult == null) {
                        // 获取口令失败, 抛出内部异常, 获取口令的接口还未开发
                        throw new RuntimeException("获取口令的接口还未开发");
                    } else {
                        String guidStr = jsonResult.getString("guid");
                        if (StringUtils.isBlank(guidStr)) {
                            // 获取口令异常，记录异常信息
                            String messageStr = jsonResult.getString("message");
                            throw new RuntimeException("获取口令异常-->" + messageStr);
                        } else {
                            originalValue = guidStr;
                        }
                    }
                }
                paramObject.put(info.getParamname(), originalValue);
            }
        }
        return paramObject;
    }

}
