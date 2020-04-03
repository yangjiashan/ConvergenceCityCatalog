package com.fgi.city.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fgi.city.aspect.Log;
import com.fgi.city.bo.InterfaceCityBO;
import com.fgi.city.bo.InterfaceLogBO;
import com.fgi.city.bo.OrgBO;
import com.fgi.city.bo.MethodInfoBO;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.CityQueryMapper;
import com.fgi.city.dao.SecretKeyMapper;
import com.fgi.city.entity.*;
import com.fgi.city.enums.FailReasonEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.service.CityQueryService;
import com.fgi.city.utils.NETUserSignonUtil;
import com.fgi.city.utils.SM2Util;
import com.fgi.city.utils.Sm4Util;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class CityQueryServiceImpl implements CityQueryService {
    private transient Logger logger = LoggerFactory.getLogger(CityQueryServiceImpl.class);
    @Autowired
    private NETUserSignonUtil netUserSignonUtil;

    @Autowired
    private SecretKeyMapper secretKeyMapper;

    @Autowired
    private CityQueryMapper cityQueryMapper;

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private HttpRequestService httpRequestService;

    /**
     * 查询接口列表
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceListGroup", commandKey = "DealInterfaceListQuery", fallbackMethod = "fallBack_queryInterface")
    public void queryInterfaceList(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            UserBean users = netUserSignonUtil.getUserInfo(guid);
            // 添加日志用
            jsonData.put(configBean.getParsed_data(), "");
            jsonData.put(configBean.getParsed_user(), users);
            // 获取该用户所属结构的SM4秘钥
            SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(users.getOrganizationID());
            if (secretKeyBean == null || StringUtils.isBlank(secretKeyBean.getSecretkey())) {
                // 该用户所属的机构还未配置SM4秘钥
                result.put("message", FailReasonEnum.FAIL_15.getDesc());
                return;
            }
            // 查询接口列表
            List<OrgBO> interfaceList = cityQueryMapper.queryInterfaceList(); //省平台自定义的接口
            List<OrgBO> interfaceCityList = cityQueryMapper.queryInterfaceCityList();// 地市的接口
            // 地址发布接口只是保存后缀"/xxx",必须拼接上前缀，http拼接 http://localhost:10010/ConvergenceCityCatalog
            // webservice拼接 http://localhost:10010/ConvergenceCityCatalog/ws
            // 2019/11/14 修改
            interfaceCityList = setInterfacePubUrl(interfaceCityList);
            // 去除空接口
            List<OrgBO> interfaceAllList = new ArrayList<>();
            interfaceAllList.addAll(interfaceList);
            interfaceAllList.addAll(interfaceCityList);
            String resultStr = JSON.toJSONString(interfaceAllList);
            JSONObject resMsg = new JSONObject();
            // 加密、签名结果
            if (!encriptAndSign(resultStr, secretKeyBean.getSecretkey(), "SM4/ECB/PKCS5Padding", result, resMsg))
                return;
            // 操作成功
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
            result.put("data", resMsg.getString("encriptMsg"));
            result.put("sign", resMsg.getString("signMessage"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", ResultStatusEnum.FAILURE100.getDesc());
        }
    }

    /**
     * 查询接口信息
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceInfoGroup", commandKey = "DealInterfaceInfoQuery", fallbackMethod = "fallBack_queryInterface")
    public void queryInterfaceInfo(JSONObject jsonData, JSONObject result) {
        try {
            // 验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            UserBean users = netUserSignonUtil.getUserInfo(guid);
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("interfaceid"), result))
                return;
            String interfaceId = data.getString("interfaceid");
            InterfaceCityBean interfaceCityBean = null;
            List<InterfaceCityBean> tempList = cityQueryMapper.queryInterfaceCityInfo(interfaceId);
            if (tempList == null || tempList.size() == 0 || tempList.size() > 1) {
                // 查询不到http接口,http接口 接口与方法一对一
                // 接口ID不存在
                result.put("message", FailReasonEnum.FAIL_41.getDesc());
                return;
            } else {
                interfaceCityBean = tempList.get(0);
            }
            InterfaceCityBO interfaceCityBO = new InterfaceCityBO(interfaceCityBean.getInterface_name(), interfaceCityBean.getInterface_url(),
                    interfaceCityBean.getInterface_desc(), interfaceCityBean.getInterface_type(), interfaceCityBean.getRequest_type(),
                    interfaceCityBean.getParam_format());
            // 地址发布接口只是保存后缀"/xxx",必须拼接上前缀，http拼接 http://localhost:10010/ConvergenceCityCatalog
            // 2019/11/14 修改
            interfaceCityBO.setInterface_url(configBean.getInterfaceExecuteUrl() + interfaceCityBO.getInterface_url());
            setInAndOutPutParamsCity(interfaceCityBO, interfaceCityBean.getMethodid(), interfaceCityBean);
            String resultStr = JSONObject.toJSONString(interfaceCityBO);
            SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(users.getOrganizationID());
            JSONObject resMsg = new JSONObject();
            // 加密、签名结果
            if (!encriptAndSign(resultStr, secretKeyBean.getSecretkey(), "SM4/ECB/PKCS5Padding", result, resMsg))
                return;
            // 操作成功
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
            result.put("data", resMsg.getString("encriptMsg"));
            result.put("sign", resMsg.getString("signMessage"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", ResultStatusEnum.FAILURE100.getDesc());
        }

    }

    /**
     * 查询方法信息
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceMethodListGroup", commandKey = "DealInterfaceMethodListQuery", fallbackMethod = "fallBack_queryInterface")
    public void queryInterfaceMethodList(JSONObject jsonData, JSONObject result) {
        try {
            // 验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            UserBean users = netUserSignonUtil.getUserInfo(guid);
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("methodid"), result))
                return;
            String methodId = data.getString("methodid");
            String type = "1";
            CustomizationInterfaceMethodBean methodInfo = cityQueryMapper.queryInterfaceMethodByMethodID(methodId);
            if (methodInfo == null) {
                CustomizationInterfaceMethodBean methodInfoCity = cityQueryMapper.queryInterfaceMethodCityByMethodID(methodId);
                if (methodInfoCity == null || "0".equals(methodInfoCity.getInterfacetype())) {
                    // 方法ID不存在
                    result.put("message", FailReasonEnum.FAIL_31.getDesc());
                    return;
                } else {
                    type = "2";
                    methodInfo = methodInfoCity;
                }
            }
            MethodInfoBO methodInfoBO = new MethodInfoBO(methodInfo.getMethod_ename(), methodInfo.getMethod_cname()
                    , methodInfo.getMethod_type(), methodInfo.getMethod_desc(), methodInfo.getRelationship());
            setInAndOutPutParams(methodInfoBO, methodId, methodInfo, type);
            String resultStr = JSONObject.toJSONString(methodInfoBO);
            SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(users.getOrganizationID());
            JSONObject resMsg = new JSONObject();
            // 加密、签名结果
            if (!encriptAndSign(resultStr, secretKeyBean.getSecretkey(), "SM4/ECB/PKCS5Padding", result, resMsg))
                return;
            // 操作成功
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
            result.put("data", resMsg.getString("encriptMsg"));
            result.put("sign", resMsg.getString("signMessage"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", ResultStatusEnum.FAILURE100.getDesc());
        }

    }

    /**
     * 查询接口调用日志，地市查询属于本市的“省平台封装”接口调用日志
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceLogGroup", commandKey = "DealInterfaceLogQuery", fallbackMethod = "fallBack_queryInterface")
    public void queryInterfaceLog(JSONObject jsonData, JSONObject result) {
        try {
            // 验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("interface_name",
                    "begin_time", "end_time", "page"), result))
                return;
            // 验证begin time, end time是否超过七天
            if (!velifyDate(data, result))
                return;
            int pageSize = 1000;
            // 获取guid对应的机构ID
            String sgID = data.getString(configBean.getParsed_sgid());
            Map<String, String> orgInfoMap = cityQueryMapper.queryAreaCodeBySGID(sgID);
            if (orgInfoMap == null || StringUtils.isBlank(orgInfoMap.get("ID"))) {
                // 请先上报机构
                result.put("code", ResultStatusEnum.FAILURE110.getCode());
                result.put("message", ResultStatusEnum.FAILURE110.getDesc());
                return;
            }
            String areaCode = orgInfoMap.get("AREACODE");
            data.put("areacode", areaCode);
            int totalCount = cityQueryMapper.queryInterfaceLogCount(data);
            // 每1000条一页
            int totalPage = calcTotalPageCount(totalCount, pageSize);
            int pageNow = getRequestPage(data.getString("page"), totalPage);
            int begin = (pageNow - 1) * pageSize + 1;
            int end = pageNow * pageSize;
            Map<String, Object> map = new HashMap<>();
            map.put("begin", begin);
            map.put("end", end);
            map.put("interface_name", data.getString("interface_name"));
            map.put("method_ename", data.getString("method_ename"));
            map.put("begin_time", data.getString("begin_time"));
            map.put("end_time", data.getString("end_time"));
            map.put("areacode", areaCode);
            List<Map<String, String>> resultData = cityQueryMapper.queryInterfaceLogPage(map);
            InterfaceLogBO interfaceLogBO = new InterfaceLogBO();
            interfaceLogBO.setCounts(String.valueOf(totalCount));
            interfaceLogBO.setPage_count(String.valueOf(totalPage));
            interfaceLogBO.setPage_now(String.valueOf(pageNow));
            // 将resultData key转为小写
            interfaceLogBO.setDetails(setLowCases(resultData));
            String jsonStr = JSONObject.toJSONString(interfaceLogBO);
            SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(data.getString(configBean.getParsed_sgid()));
            JSONObject resMsg = new JSONObject();
            // 加密、签名结果
            if (!encriptAndSign(jsonStr, secretKeyBean.getSecretkey(), "SM4/ECB/PKCS5Padding", result, resMsg))
                return;
            // 操作成功
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
            result.put("data", resMsg.getString("encriptMsg"));
            result.put("sign", resMsg.getString("signMessage"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", ResultStatusEnum.FAILURE100.getDesc());
        }
    }

    // 重新设置接口发布地址
    private List<OrgBO> setInterfacePubUrl(List<OrgBO> interfaceCityList) {
        if (interfaceCityList != null) {
            for (OrgBO orgBO : interfaceCityList) {
                List<CustomizationInterfaceBean> interfaces = orgBO.getInterfaces();
                if (interfaces != null) {
                    for (CustomizationInterfaceBean cb : interfaces) {
                        if ("1".equals(cb.getInterfacetype())) {
                            // webservice
                            cb.setInterface_url(configBean.getInterfaceExecuteUrl() + "/ws" + cb.getInterface_url() + "?wsdl");
                        } else if ("0".equals(cb.getInterfacetype())) {
                            // http
                            cb.setInterface_url(configBean.getInterfaceExecuteUrl() + cb.getInterface_url());
                        }
                    }
                }
            }
        }
        return interfaceCityList;
    }

    // 将List<Map> key转为小写
    private List<Map<String, String>> setLowCases(List<Map<String, String>> details) {
        List<Map<String, String>> mapList = new ArrayList<>();
        if (details != null) {
            for (Map<String, String> map : details) {
                Map<String, String> mapTemp = new HashMap<>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String keys = entry.getKey();
                    if (StringUtils.isBlank(keys)) {
                        continue;
                    }
                    keys = keys.toLowerCase();
                    String values = entry.getValue();
                    mapTemp.put(keys, values);
                }
                mapList.add(mapTemp);
            }
            return mapList;
        }
        return null;
    }

    // 获取页数
    public int getRequestPage(String pageStr, int totalPage) {
        int page = 1;
        try {
            int pageTemp = Integer.parseInt(pageStr);
            if (pageTemp <= totalPage && pageTemp >= 1) {
                page = pageTemp;
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return page;
    }

    // 取得总页数，总页数=总记录数/总页数
    public int calcTotalPageCount(int totalCount, int pageSize) {
        int totalPageCount = totalCount / pageSize;
        totalPageCount = (totalCount % pageSize == 0) ? totalPageCount : totalPageCount + 1;
        return totalPageCount;
    }

    // 验证日期格式并且是否大于7天
    private boolean velifyDate(JSONObject jsonData, JSONObject result) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            String begin_time = jsonData.getString("begin_time");
            String end_time = jsonData.getString("end_time");
            Date date_begin = simpleDateFormat.parse(begin_time);
            Date date_end = simpleDateFormat.parse(end_time);
            int days = (int) ((date_end.getTime() - date_begin.getTime()) / (1000 * 3600 * 24));
            if (days <= 7) {
                return true;
            }
            // 日期超过七天
            result.put("message", FailReasonEnum.FAIL_34.getDesc());
        } catch (Exception e) {
            // 解析日期格式出错
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_35.getDesc());
        }
        return false;
    }

    // 设置返回值示例
    private JSONObject switchValues(List<MethodFieldBean> methodFieldOutPut, int type, String resultExample) {
        JSONObject result = new JSONObject();
        JSONArray data = new JSONArray();
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        if (type == 0) {
            JSONObject temp = new JSONObject();
            if (methodFieldOutPut != null) {
                for (MethodFieldBean info : methodFieldOutPut) {
                    temp.put(info.getParam_ename(), "xxxxx");
                }
            }
            data.add(temp);
            result.put("data", data);
        } else {
            result.put("data", resultExample);
        }
        return result;
    }

    // 设置返回值示例
    private JSONObject switchCityValues(List<MethodFieldBean> methodFieldOutPut, int type, String resultExample) {
        JSONObject result = new JSONObject();
        JSONArray data = new JSONArray();
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        if (type == 0) {
            JSONObject temp = new JSONObject();
            if (methodFieldOutPut != null) {
                for (MethodFieldBean info : methodFieldOutPut) {
                    temp.put(info.getParam_name(), "xxxxx");
                }
            }
            data.add(temp);
            result.put("data", data);
        } else {
            result.put("data", resultExample);
        }
        return result;
    }


    // 设置方法输入、输出参数
    private void setInAndOutPutParams(MethodInfoBO methodInfoBO, String methodId, CustomizationInterfaceMethodBean methodInfo, String type) {
        MethodFieldBean params = new MethodFieldBean();
        params.setMethodid(methodId);
        if ("1".equals(type)) {
            // 查询省平台
            // 0表示自动生成接口，1表示定制接口，需要人工介入开发
            if ("0".equals(methodInfo.getInterfacetype())) {
                // 查询输入参数
                params.setFieldtype("0");
                List<MethodFieldBean> methodFieldInput = cityQueryMapper.queryMethodFieldByMethodIDAuto(params);
                if (methodFieldInput == null) {
                    methodFieldInput = new ArrayList<>();
                }
                methodInfoBO.setInput_parameters(methodFieldInput);
                // 方法类型：0、查询类，1、验证类
                if ("0".equals(methodInfo.getMethod_type())) {
                    params.setFieldtype("1");// 查询返回值参数
                    List<MethodFieldBean> methodFieldOutPut = cityQueryMapper.queryMethodFieldByMethodIDAuto(params);
                    if (methodFieldOutPut == null) {
                        methodFieldOutPut = new ArrayList<>();
                    }
                    methodInfoBO.setOut_parameters(methodFieldOutPut);
                    // 返回示例
                    methodInfoBO.setResult_example(switchValues(methodFieldOutPut, 0, null));
                } else {
                    // 返回值参数为 true or false , 放空
                    methodInfoBO.setOut_parameters(new ArrayList<>());
                    // 返回示例
                    methodInfoBO.setResult_example(switchValues(null, 1, "true"));
                }
            } else if ("1".equals(methodInfo.getInterfacetype())) {
                // 1表示定制接口，需要人工介入开发
                // 查询输入参数
                List<MethodFieldBean> methodFieldInPut = cityQueryMapper.queryMethodFieldByMethodID(methodId);
                methodInfoBO.setInput_parameters(methodFieldInPut);
                // 输出参数放空
                methodInfoBO.setOut_parameters(new ArrayList<>());
                // 返回值示例
                methodInfoBO.setResult_example(switchValues(null, 1, methodInfo.getResult_example()));
            }
        } else {
            // 地市
            params.setFieldtype("0");
            List<MethodFieldBean> methodFieldInput = cityQueryMapper.queryMethodFieldCityByMethodIDAuto(params);
            if (methodFieldInput == null) {
                methodFieldInput = new ArrayList<>();
            }
            methodInfoBO.setInput_parameters(methodFieldInput);
            // 方法类型：0、查询类，1、验证类
            if ("0".equals(methodInfo.getMethod_type())) {
                params.setFieldtype("1");// 查询返回值参数
                List<MethodFieldBean> methodFieldOutPut = cityQueryMapper.queryMethodFieldCityByMethodIDAuto(params);
                if (methodFieldOutPut == null) {
                    methodFieldOutPut = new ArrayList<>();
                }
                methodInfoBO.setOut_parameters(methodFieldOutPut);
                // 返回示例
                methodInfoBO.setResult_example(switchValues(methodFieldOutPut, 0, null));
            } else {
                // 返回值参数为 true or false , 放空
                methodInfoBO.setOut_parameters(new ArrayList<>());
                // 返回示例
                methodInfoBO.setResult_example(switchValues(null, 1, "true"));
            }
        }
    }

    // 设置方法输入、输出参数
    private void setInAndOutPutParamsCity(InterfaceCityBO interfaceCityBO, String methodId, InterfaceCityBean cityBean) {
        MethodFieldBean params = new MethodFieldBean();
        params.setMethodid(methodId);
        // 地市
        params.setFieldtype("0");
        List<MethodFieldBean> methodFieldInput = cityQueryMapper.queryHttpFieldCityByMethodIDAuto(params);
        if (methodFieldInput == null) {
            methodFieldInput = new ArrayList<>();
        }
        interfaceCityBO.setInput_parameters(methodFieldInput);
        // 方法类型：0、查询类，1、验证类
        if ("0".equals(cityBean.getInterface_type())) {
            params.setFieldtype("1");// 查询返回值参数
            List<MethodFieldBean> methodFieldOutPut = cityQueryMapper.queryHttpFieldCityByMethodIDAuto(params);
            if (methodFieldOutPut == null) {
                methodFieldOutPut = new ArrayList<>();
            }
            interfaceCityBO.setOut_parameters(methodFieldOutPut);
            // 返回示例
            interfaceCityBO.setResult_example(switchCityValues(methodFieldOutPut, 0, null));
        } else {
            // 返回值参数为 true or false , 放空
            interfaceCityBO.setOut_parameters(new ArrayList<>());
            // 返回示例
            interfaceCityBO.setResult_example(switchCityValues(null, 1, "true"));
        }
    }

    // 加密、签名
    private boolean encriptAndSign(String message, String SM4Key, String algorithmname, JSONObject result, JSONObject encriptResMsg) {
        String encriptMsg = Sm4Util.getInstance().SM4Encrypt(message, algorithmname, SM4Key, null);
        if (encriptMsg == null) {
            result.put("code", ResultStatusEnum.FAILURE07.getCode());
            result.put("message", ResultStatusEnum.FAILURE07.getDesc());
            return false;
        }
        String signMessage = null;
        try {
            signMessage = SM2Util.getInstance().SM2Sign(encriptMsg, configBean.getHjsm2prikey());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_30);
            return false;
        }
        encriptResMsg.put("encriptMsg", encriptMsg);
        encriptResMsg.put("signMessage", signMessage);
        return true;
    }

//    public List<OrgBO> removeEmptyInterface(List<OrgBO> interfaceList){
//        List<OrgBO> newInterfaceList = new ArrayList<>();
//        if (interfaceList != null) {
//            for (OrgBO org :interfaceList) {
//                List<CustomizationInterfaceBean> interfaces = org.getInterfaces();
//
//                for (CustomizationInterfaceBean cb: interfaces) {
//                    String interfaceType = cb.getInterfacetype();
//                    if ("01".equals(interfaceType)) {
//
//
//
//                    } else {
//
//
//                    }
//
//                }
//
//
//
//            }
//        }
//        List<OrgBO> interfaceList
//
//    }

    // 降级
    private void fallBack_queryInterface(JSONObject jsonData, JSONObject result) {
        result.put("message", FailReasonEnum.FAIL_17.getDesc());
        logger.info(result.toJSONString());
    }
}
