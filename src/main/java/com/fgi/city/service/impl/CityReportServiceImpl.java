package com.fgi.city.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fgi.city.aspect.Log;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.CityQueryMapper;
import com.fgi.city.dao.CityReportMapper;
import com.fgi.city.entity.DataPreviewInfo;
import com.fgi.city.entity.UserBean;
import com.fgi.city.enums.*;
import com.fgi.city.service.CityReportService;
import com.fgi.city.utils.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import java.util.*;

@Service
public class CityReportServiceImpl implements CityReportService {

    private transient Logger logger = LoggerFactory.getLogger(CityReportServiceImpl.class);

    @Autowired
    private HttpRequestService httpRequestService;

    @Autowired
    private CityReportMapper cityReportMapper;

    @Autowired
    private CityQueryMapper cityQueryMapper;

    @Autowired
    private RollBackService rollBackService;

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private QueryAreaCodeUtil queryAreaCodeUtil;

    @Override
    public int getCountByOrgName(String orgname) {
        return cityReportMapper.getCountByOrgName(orgname);
    }

    /**
     * 处理机构上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealOrgGroup", commandKey = "DealOrgReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithOrgReport(JSONObject jsonData, JSONObject result) {
        // 解密并且验证guid有效性
        try {
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_orgname", "orgname", "orgshortname", "areacode", "sortnum"), result))
                return;
            // 验证guid所属地市是否是与上报的areacode同一个地市
            String areaCode = data.getString("areacode");
            String areaCodeId = queryAreaCodeUtil.getAreaCodeID(areaCode);
            if (StringUtils.isBlank(areaCodeId)) {
                // 请输入正确的地市编码
                result.put("message", FailReasonEnum.FAIL_42.getDesc());
                return;
            }
            // 查询
            String SingleOrgID = data.getString(configBean.getParsed_sgid());
            if (!validateOrg_AareCode(SingleOrgID, areaCodeId)) {
                result.put("code", ResultStatusEnum.FAILURE117.getCode());
                result.put("message", ResultStatusEnum.FAILURE117.getDesc());
                return;
            }
            String operator = data.getString("opetype");
            synchronized (this) {
                // 2019/10/31 黄芳对接时出现并发问题 暂且先添加同步锁
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 新增或者更新
                    if (getCountByOrgName(data.getString("query_orgname")) > 0) {
                        // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
                        String query_orgname = data.getString("query_orgname");
                        String orgname = data.getString("orgname");
                        data.put("orgname", query_orgname);
                        if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                            return;
                        data.put("orgname", orgname);
                        updateOrgCity(data, result);
                    } else {
                        addOrgCity(data, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 删除
                    // 验证可操作性（可修改的数据必须是guid所属的机构下，防止跨机构篡改数据）
                    if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                        return;
                    // 判断是否存在该条数据
                    if (getCountByOrgName(data.getString("query_orgname")) <= 0) {
                        // 不存在，错误返回
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_43.getDesc());
                        return;
                    }
                    rollBackService.deleteOrgCity(data, result);
                } else {
                    result.put("message", FailReasonEnum.FAIL_01.getDesc() + ":opetype -> addOrUpdate or delete");
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理机构图标上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealOrgIconGroup", commandKey = "DealOrgIconReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithOrgIconReport(JSONObject jsonData, JSONObject result) {
        // 解密并且验证guid有效性
        try {
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("orgname", "icon"), result))
                return;
            // 判断机构是否存在
            int orgExist = getCountByOrgName(data.getString("orgname"));
            if (orgExist <= 0) {
                // 机构不存在，无法关联
                result.put("code", ResultStatusEnum.FAILURE101.getCode());
                result.put("message", ResultStatusEnum.FAILURE101.getDesc());
                return;
            }
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证图片大小（60*60(误差5) png格式 大小不能超过1M）
            String iconStr = data.getString("icon");
            // -1:文件大小不符合要求, -2:文件宽高不符合要求, -3:文件格式不符合要求, -4:文件解析错误
            int checkResult = ImageUtil.getInstance().checkFormat(iconStr, 1, 55, 65, "png");
            if (checkResult == -1) {
                result.put("code", ResultStatusEnum.FAILURE106.getCode());
                result.put("message", ResultStatusEnum.FAILURE106.getDesc());
                return;
            } else if (checkResult == -2) {
                result.put("code", ResultStatusEnum.FAILURE107.getCode());
                result.put("message", ResultStatusEnum.FAILURE107.getDesc());
                return;
            } else if (checkResult == -3) {
                result.put("code", ResultStatusEnum.FAILURE108.getCode());
                result.put("message", ResultStatusEnum.FAILURE108.getDesc());
                return;
            } else if (checkResult == -4) {
                result.put("code", ResultStatusEnum.FAILURE109.getCode());
                result.put("message", ResultStatusEnum.FAILURE109.getDesc());
                return;
            }
            // 保存机构图标
            String tempIconStr = ImageUtil.getInstance().replacePre(data.getString("icon"), "png");
            byte[] iconArray = new BASE64Decoder().decodeBuffer(tempIconStr);
            data.put("icon", iconArray);
            cityReportMapper.updateOrgIcon(data);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理业务系统上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealSysGroup", commandKey = "DealSysReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithSysReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_sysname",
                    "orgname", "sysname", "run_network", "databasetype", "sysintroduction", "contactdept", "contactperson", "phone",
                    "developer_contactdept", "developer_contactperson", "developer_phone", "sortnum"), result))
                return;
            String operator = data.getString("opetype");
            if (!verifyOperator(operator, result))
                return;
            // 验证上级机构是否存在
            String orgId = verifyOrgIDExist(data.getString("orgname"), result);
            if (orgId == null)
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            data.put("orgid", orgId);
            synchronized (this) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 2019/10/31 黄芳对接时出现并发问题 暂且先添加同步锁
                    // 新增或者更新
                    if (cityReportMapper.getCountByQuerySysNameAndOrgId(data) > 0) {
                        // 修改
                        updateSysCity(data, result);
                    } else {
                        // 添加
                        addSysCity(data, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 删除
                    // 判断业务系统是否存在
                    if (cityReportMapper.countSysCity(data) <= 0) {
                        // 业务系统不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_36.getDesc());
                        return;
                    }
                    rollBackService.deleteSysCity(data, result);
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理业务事项
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealCatalogGroup", commandKey = "DealCatalogReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithCatalogReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_catalogname",
                    "orgname", "catalogname", "sortnum"), result))
                return;
            // 判断业务系统是不是上报了
            if (!data.containsKey("sysname")) {
                result.put("code", ResultStatusEnum.FAILURE06.getCode());
                result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + "sysname");
                return;
            }
            String operator = data.getString("opetype");
            if (!verifyOperator(operator, result))
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证上级机构,上级业务系统是否存在
            String sysId = verifySysIdExist(data, result);
            if (sysId == null)
                return;
            data.put("sysid", sysId);
            synchronized (this) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 新增或者更新
                    if (cityReportMapper.getCatalogCountBySysIDAndCatalogName(data) > 0) {
                        // 修改
                        updateCatalogCity(data, result);
                    } else {
                        // 添加
                        addCatalogCity(data, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 判断业务事项是否存在
                    if (cityReportMapper.countCatalogCity(data) <= 0) {
                        // 业务事项不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_37.getDesc());
                        return;
                    }
                    // 删除
                    rollBackService.deleteCatalogCity(data, result);
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理业务信息上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealMatterGroup", commandKey = "DealMatterReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithMatterReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_businessmatter_cname",
                    "orgname", "catalogname", "businessmatter_cname", "businessmatter_ename", "infotype", "sortnum"), result))
                return;
            // 判断业务系统是不是上报了
            if (!data.containsKey("sysname")) {
                result.put("code", ResultStatusEnum.FAILURE06.getCode());
                result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + "sysname");
                return;
            }
            String operator = data.getString("opetype");
            if (!verifyOperator(operator, result))
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证业务事项是否存在
            String catalogId = verifyCatalogIDExist(data, result);
            if (catalogId == null)
                return;
            data.put("catalogid", catalogId);
            synchronized (this) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 新增或者更新
                    if (cityReportMapper.getMatterCountByMatternameAndCatalogId(data) > 0) {
                        // 修改
                        updateMatterCity(data, result);
                    } else {
                        // 添加
                        addMatterCity(data, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 判断业务信息是否存在
                    if (cityReportMapper.countMatterCity(data) <= 0) {
                        // 业务信息不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_38.getDesc());
                        return;
                    }
                    // 删除
                    rollBackService.deleteMatterCity(data, result);
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理业务指标项上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealMatterIndicatorGroup", commandKey = "DealMatterIndicatorReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithMatterIndicatorReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            String matterId = null;
            // 验证必填项
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("orgname", "catalogname",
                    "businessmatter_cname", "indicatorinfo"), result))
                return;
            // 判断业务系统是不是上报了
            if (!data.containsKey("sysname")) {
                result.put("code", ResultStatusEnum.FAILURE06.getCode());
                result.put("message", FailReasonEnum.FAIL_18.getDesc() + ":" + "sysname");
                return;
            }
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证业务信息是否存在
            matterId = verifyMatterIDExist(data, result);
            if (matterId == null)
                return;
            // 解析指标
            JSONArray indicators = parseIndicator(data, result);
            if (null == indicators)
                return;
            // 处理指标
            synchronized (this) {
                if (rollBackService.dealWithIndicator(matterId, indicators, result)) {
                    // 返回正常
                    result.put("code", ResultStatusEnum.SUCCUSS.getCode());
                    result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
                    return;
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        if (StringUtils.isBlank(result.getString("message"))) {
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", ResultStatusEnum.FAILURE100.getDesc());
        }
    }

    /**
     * 处理业务信息预览数据上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealMatterDataGroup", commandKey = "DealMatterDataReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithMatterDataReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("orgname", "catalogname", "businessmatter_cname", "data"), result))
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证业务信息是否存在
            String matterId = verifyMatterIDExist(data, result);
            if (matterId == null)
                return;
            data.put("matterid", matterId);
            // 验证data数据
            JSONArray dataPreview = parseMatterData(data, result);
            if (null == dataPreview)
                return;
            // 删除原来数据，插入新数据
            rollBackService.dealMatterDataPreview(dataPreview, matterId);
            // 操作成功
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理业务数据预览接口上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealWithMatterDataUrlGroup", commandKey = "DealWithMatterDataUrl", fallbackMethod = "fallBack_orgReport")
    public void dealWithMatterDataUrlReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("areacode", "interfaceurl"), result))
                return;
            // 验证该上报者所属的机构是否存在
            String singleId = data.getString(configBean.getParsed_sgid());
            List<Map<String, String>> maps = cityReportMapper.queryOrganizationBySgId(singleId);
            if (maps == null || maps.size() <= 0) {
                // 还不存在上报者所属的机构，请先上报机构！
                result.put("code", ResultStatusEnum.FAILURE110.getCode());
                result.put("message", ResultStatusEnum.FAILURE110.getDesc());
                return;
            }
            if (!listMapContainsStr(maps, data.getString("areacode"))) {
                // 上报者所处的地市与要修改的地市不一致，修改失败！
                result.put("code", ResultStatusEnum.FAILURE111.getCode());
                result.put("message", ResultStatusEnum.FAILURE111.getDesc());
                return;
            }
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            DataPreviewInfo dataPreviewInfo = new DataPreviewInfo();
            dataPreviewInfo.setId(IdGenerateUtil.getKey());
            dataPreviewInfo.setAreacode(data.getString("areacode"));
            dataPreviewInfo.setDataurl(data.getString("interfaceurl"));
            dataPreviewInfo.setReportaccount(users.getAccount());
            dataPreviewInfo.setReportorg(users.getOrganizationID());
            synchronized (this) {
                // 删除原来数据，插入新数据
                DataPreviewInfo dataPreviewInfoTemp = cityReportMapper.queryDataPreviewByAreaCode(data.getString("areacode"));
                if (dataPreviewInfoTemp != null && !StringUtils.isBlank(dataPreviewInfoTemp.getAreacode())) {
                    // 更新
                    cityReportMapper.updateDataPreviewUrl(dataPreviewInfo);
                } else {
                    // 添加
                    cityReportMapper.insertDataPreviewUrl(dataPreviewInfo);
                }
            }
            // 操作成功
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    // 判断listmap是否包含某个字符串
    private boolean listMapContainsStr(List<Map<String, String>> maps, String str) {
        for (Map<String, String> map : maps) {
            String areaCode = map.get("AREACODE");
            if (str.equals(areaCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理地市接口上报（webservice wsdl）
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceGroup", commandKey = "DealInterfaceReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithInterfaceReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            // 2019/11/30 新增接口简称为必填参数
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_interface_name",
                    "orgname", "interface_name", "interface_sname", "interface_url", "interface_desc", "sortnum"), result))
                return;
            String operator = data.getString("opetype");
            if (!verifyOperator(operator, result))
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证机构是否存在
            String orgId = verifyOrgIDExist(data.getString("orgname"), result);
            if (null == orgId)
                return;
            data.put("orgid", orgId);
            // 设置webservice类型
            data.put("interface_type", "01");
            synchronized (this) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 新增或者更新
                    if (cityReportMapper.getInterfaceCountByOrgIDAndName(data) > 0) {
                        // 修改
                        rollBackService.updateInterfaceCity(data, result);
                    } else {
                        // 添加
                        rollBackService.addInterfaceCity(data, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 删除
                    Map<String, String> interfaceOldInfo = cityReportMapper.queryInterfaceInfoByName(data);
                    if (interfaceOldInfo == null) {
                        // 接口不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_39.getDesc());
                        return;
                    }
                    rollBackService.deleteInterfaceCity(data, result);
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理地市接口方法上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceMethodGroup", commandKey = "DealInterfaceMethodReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithInterfaceMethodReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_method",
                    "orgname", "interface_name", "method", "method_cname", "method_describe", "method_type", "querytable", "sortnum", "result_example"), result))
                return;
            String operator = data.getString("opetype");
            if (!verifyOperator(operator, result))
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证接口是否存在
            String interfaceId = verifyInterfaceIDExist(data, result);
            if (null == interfaceId)
                return;
            data.put("interfaceid", interfaceId);
            // 验证输入参数，要么不传，要么格式要对
            JSONArray parsedParams = new JSONArray();
            if (!parseInputParams(data, result, parsedParams))
                return;
            // 验证输出参数，要么不传，要么格式要对
            JSONArray parsedOutParams = new JSONArray();
            if (!parseOutParams(data, result, parsedOutParams))
                return;
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
            logger.info("看下输入参数：" + parsedParams.toJSONString());
            logger.info("看下输出参数：" + parsedOutParams.toJSONString());
            synchronized (this) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 新增或者更新
                    if (cityReportMapper.getInterfaceMethodCountByInterfaceidAndMethodName(data) > 0) {
                        // 修改
                        rollBackService.updateInterfaceMethod(data, parsedParams, parsedOutParams);
                    } else {
                        // 添加
                        rollBackService.addInterfaceMethod(data, parsedParams, parsedOutParams, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 删除
                    String methodId = cityReportMapper.getInterfaceMethodIDByInterfaceIDAndMethodName(data);
                    if (StringUtils.isBlank(methodId)) {
                        // 接口方法不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_40.getDesc());
                        return;
                    }
                    rollBackService.deleteInterfaceMethod(data, result);
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理地市接口上报(http restful格式)
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealHttpInterfaceGroup", commandKey = "DealHttpInterfaceReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithHttpInterfaceReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            // 2019/11/30 添加参数接口简称必填
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("opetype", "query_interface_name",
                    "orgname", "interface_name", "interface_sname", "interface_url", "interface_desc", "querytable", "operation_type", "request_type", "param_format", "sortnum", "result_example"), result))
                return;
            String operator = data.getString("opetype");
            if (!verifyOperator(operator, result))
                return;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return;
            // 验证机构是否存在
            String orgId = verifyOrgIDExist(data.getString("orgname"), result);
            if (null == orgId)
                return;
            // 解析输入、输出参数
            JSONArray inputParams = new JSONArray();
            if (!parseHttpParams(data, result, inputParams, "input_params"))
                return;
            JSONArray outParams = new JSONArray();
            if (!parseHttpParams(data, result, outParams, "out_params"))
                return;
            data.put("orgid", orgId);
            // 设置http restful 类型
            data.put("interface_type", "02");
            synchronized (this) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(operator)) {
                    // 新增或者更新
                    if (cityReportMapper.getInterfaceCountByOrgIDAndName(data) > 0) {
                        // 修改
                        rollBackService.updateHttpInterfaceCity(data, inputParams, outParams, result);
                    } else {
                        // 添加
                        rollBackService.addHttpInterfaceCity(data, inputParams, outParams, result);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(operator)) {
                    // 删除
                    Map<String, String> interfaceOldInfo = cityReportMapper.queryInterfaceInfoByName(data);
                    if (interfaceOldInfo == null) {
                        // 接口不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", FailReasonEnum.FAIL_39.getDesc());
                        return;
                    } else {
                        rollBackService.deleteInterfaceCity(data, result);
                    }
                }
            }
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理账号密码上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealAccountAndPwdGroup", commandKey = "DealAccountAndPwdReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithAccountAndPwdReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("username", "password"), result))
                return;
            String sgID = data.getString(configBean.getParsed_sgid());
            // 获取当前机构所在的地区码
            Map<String, String> orgInfoMap = cityQueryMapper.queryAreaCodeBySGID(sgID);
            if (orgInfoMap == null || StringUtils.isBlank(orgInfoMap.get("ID"))) {
                // 请先上报机构
                result.put("code", ResultStatusEnum.FAILURE110.getCode());
                result.put("message", ResultStatusEnum.FAILURE110.getDesc());
                return;
            }
            String areaCode = orgInfoMap.get("AREACODE");
            data.put("areacode", areaCode);
            // 查看是否存在获取口令接口名称的参数
            String methodId = getTokenInterfaceId(data, result);
            if (methodId == null) {
                // 异常
                return;
            }
            data.put("orgid", sgID);
            // 验证通过，保存账号密码
            data.put("methodid", methodId);
            rollBackService.saveAccountAndPwdCity(data);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    /**
     * 处理日志上报
     *
     * @param jsonData
     * @param result
     */
    @Log
    @Override
    @HystrixCommand(groupKey = "DealInterfaceLogGroup", commandKey = "DealInterfaceLogReport", fallbackMethod = "fallBack_orgReport")
    public void dealWithInterfaceLogReport(JSONObject jsonData, JSONObject result) {
        try {
            // 解密并且验证guid有效性
            String guid = httpRequestService.checkGuid(jsonData, result);
            if (StringUtils.isBlank(guid))
                return;
            // 解密并且获取data数据
            JSONObject data = httpRequestService.getData(jsonData, result, guid);
            if (data == null)
                return;
            if (!httpRequestService.checkNotEmpty(data, httpRequestService.needParams("interface_id", "details"), result))
                return;
            // 验证 details 数组完整性
            if (!verifyLogs(data, result))
                return;
            // 查询接口ID和方法ID
            Map<String, String> interfaceInfo = queryInterfaceId(data, result);
            if (interfaceInfo == null)
                return;
            // 保存接口调用日志
            UserBean users = (UserBean)jsonData.get(configBean.getParsed_user());
            rollBackService.saveReportLog(data, interfaceInfo, users);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
        }
    }

    // 验证机构ID与地区码是否是同一个地市
    private boolean validateOrg_AareCode(String orgid, String areaCodeID) {
        Map<String, String> orgMap = cityReportMapper.querySgOrgId(orgid);
        return orgMap.containsValue(areaCodeID);
    }

    // 查询接口ID
    private Map<String, String> queryInterfaceId(JSONObject jsonData, JSONObject result) {
        String interface_id = jsonData.getString("interface_id");
        // 优先查询wsdl，作为方法ID， 地市查询
        Map<String, String> resultMap = cityReportMapper.getInterfaceMethodById(interface_id);
        Map<String, String> map = new HashMap<>();
        if (resultMap == null || StringUtils.isBlank(resultMap.get("METHODID")) || "02".equals(resultMap.get("INTERFACETYPE"))) {
            // 换http查询
            List<Map<String, String>> tempList = cityReportMapper.getInterfaceById(interface_id);
            if (tempList == null || tempList.size() == 0) {
                // 如果还是为空，则可能是省平台接口 . 2019/11/1 15:32分修改
//                if (resultMap == null || StringUtils.isBlank(resultMap.get("METHODID"))) {
                // 查询省平台接口
                resultMap = cityReportMapper.getHJInterfaceById(interface_id);
//                }
            } else {
                if (tempList.size() == 1) {
                    if ("02".equals(tempList.get(0).get("INTERFACETYPE"))) {
                        // 找到
                        resultMap = tempList.get(0);
                    }
                }
            }
        }
        if (resultMap != null && !StringUtils.isBlank(resultMap.get("METHODID"))) {
            // 查询到对应的接口
            map.put("interfaceid", resultMap.get("INTERFACEID"));
            map.put("methodid", resultMap.get("METHODID"));
            map.put("methodname", resultMap.get("METHODNAME"));
            map.put("interfacename", resultMap.get("INTERFACENAME"));
            map.put("interfacetype", resultMap.get("INTERFACETYPE"));
            map.put("methodename", resultMap.get("METHODENAME"));
            return map;
        }
        // 不能定位到接口
        result.put("code", ResultStatusEnum.FAILURE200.getCode());
        result.put("message", ResultStatusEnum.FAILURE200.getDesc());
        return null;
    }

    // 验证 details 数组完整性
    private boolean verifyLogs(JSONObject jsonData, JSONObject result) {
        JSONArray detailsArray = jsonData.getJSONArray("details");
        if (detailsArray != null) {
            for (int i = 0; i < detailsArray.size(); i++) {
                JSONObject json_i = detailsArray.getJSONObject(i);
                if (!httpRequestService.checkNotEmpty(json_i, httpRequestService.needParams("invoke_organization",
                        "invoke_account", "invoke_name", "invoke_time"), result))
                    return false;
            }
        } else {
            // details 不能为空
            result.put("code", ResultStatusEnum.FAILURE06.getCode());
            result.put("message", FailReasonEnum.FAIL_18.getDesc() + ": details ");
            return false;
        }
        return true;
    }

    // 根据上报参数获取口令接口ID
    private String getTokenInterfaceId(JSONObject data, JSONObject result) {
        String token_interface_name = data.getString("token_interface_name");
        String token_method_name = data.getString("token_method_name");
        // 查询该单位下的接口名称对应的接口ID
        if (StringUtils.isBlank(token_interface_name) &&
                StringUtils.isBlank(token_method_name)) {
            // 没有口令接口
            return "";
        } else {
            String type = "02"; // 默认http
            if (StringUtils.isBlank(token_interface_name)) {
                // 接口名称不能为空
                result.put("message", FailReasonEnum.FAIL_32.getDesc());
                return null;
            }
            if (!StringUtils.isBlank(token_method_name)) {
                type = "01"; // wsdl
            } else {
                data.put("token_method_name", "");
            }
            String orgname = data.getString("orgname");
            if (StringUtils.isBlank(orgname)) {
                // 机构不能为空
                result.put("message", FailReasonEnum.FAIL_45.getDesc());
                return null;
            }
            String orgId = verifyOrgIDExist(orgname, result);
            if (orgId == null)
                return null;
            // 验证可操作性（可修改的数据必须是当前guid所属的机构下，防止跨机构篡改数据）
            if (!verifyOperability(ModuleEnum.ORG_REPORT.getVal(), data, result))
                return null;
            data.put("orgid", orgId);
            data.put("interfacetype", type);
            List<Map<String, String>> results = cityReportMapper.getMethodIdByMethodName(data);
            if (results != null && results.size() == 1) {
                // 匹配到口令接口
                return results.get(0).get("METHODID");
            } else {
                // 未匹配到口令接口
                result.put("message", FailReasonEnum.FAIL_33.getDesc());
                return null;
            }
        }
    }

    // 解析接口方法输出参数
    private boolean parseOutParams(JSONObject data, JSONObject result, JSONArray parsedParams) {
        JSONArray outParams = null;
        try {
            outParams = data.getJSONArray("out_params");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":out_params");
            return false;
        }
        if (null == outParams || outParams.isEmpty()) {
            return true;
        }

        for (int i = 0; i < outParams.size(); i++) {
            JSONObject data_i = outParams.getJSONObject(i);
            // 验证必填字段
            if (!httpRequestService.checkNotEmpty(data_i, httpRequestService.needParams("param_name", "param_describe"), result)
                    || !httpRequestService.checkNeedParam(data_i, httpRequestService.needParams("param_value"), result)
            )
                return false;
            parsedParams.add(data_i);
        }
        return true;
    }

    // 解析接口方法输入参数
    private boolean parseInputParams(JSONObject data, JSONObject result, JSONArray parsedParams) {
        JSONArray inputParams = null;
        try {
            inputParams = data.getJSONArray("input_params");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":input_params");
            return false;
        }
        if (null == inputParams || inputParams.isEmpty()) {
            return true;
        }
        for (int i = 0; i < inputParams.size(); i++) {
            JSONObject data_i = inputParams.getJSONObject(i);
            // 验证必填字段
            if (!httpRequestService.checkNotEmpty(data_i, httpRequestService.needParams("param_name", "param_order", "param_describe", "param_type"), result)
                || !httpRequestService.checkNeedParam(data_i, httpRequestService.needParams("param_value"), result)
            )
                return false;
            parsedParams.add(data_i);
        }
        return true;
    }

    // 解析预览数据
    private JSONArray parseMatterData(JSONObject data, JSONObject result) {
        JSONArray matterData = null;
        try {
            matterData = data.getJSONArray("data");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":data");
            return null;
        }
        // 获取该业务信息对应的指标项
        Map<String, String> dataMap = cityReportMapper.getBusinessMatterByID(data.getString("matterid"));
        List<Map<String, String>> listData = null;
        // 关联私有事项
        listData = cityReportMapper.getBusinessIndicatorByMatterID(data.getString("matterid"));
        if (listData == null || listData.size() <= 0) {
            // 请先上报指标项
            result.put("message", FailReasonEnum.FAIL_25.getDesc());
            return null;
        }
        List<String> listIndicator = new ArrayList<>();
        for (int i = 0; i < listData.size(); i++) {
            Map<String, String> map = listData.get(i);
            listIndicator.add(map.get("ENAME"));
        }
        for (int i = 0; i < matterData.size(); i++) {
            JSONObject object_i = matterData.getJSONObject(i);
            if (null == object_i || object_i.isEmpty()) {
                result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":data");
                return null;
            } else {
                // 验证必填项
                if (!httpRequestService.checkNeedParam(object_i, httpRequestService.needParams(
                        listIndicator.toArray(new String[listIndicator.size()])), result))
                    return null;
            }
        }
        return matterData;
    }

    // 解析指标项
    private JSONArray parseIndicator(JSONObject data, JSONObject result) {
        JSONArray indicatorinfo = null;
        try {
            indicatorinfo = data.getJSONArray("indicatorinfo");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":indicatorinfo");
            return null;
        }
        for (int i = 0; i < indicatorinfo.size(); i++) {
            JSONObject data_i = indicatorinfo.getJSONObject(i);
            if (null == data_i || data_i.isEmpty()) {
                result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":indicatorinfo");
                return null;
            } else {
                // 验证必填字段
                if (!httpRequestService.checkNotEmpty(data_i, httpRequestService.needParams("query_indicator_ename", "indicator_cname", "indicator_ename",
                        "indicatortype", "indicatorlength", "ispk", "isnull", "sortnum", "opetype"), result))
                    return null;
                // 验证操作类型
                String operator = data_i.getString("opetype");
                if (!verifyOperator(operator, result))
                    return null;
            }
        }
        return indicatorinfo;
    }

    // 根据单点机构ID获取该机构所在地市码
    private String getCityCodeBySingleID(JSONObject data, JSONObject result) {
        try {
            String res = cityReportMapper.getCityCodeBySingleID(data.getString(configBean.getParsed_sgid()));
            if (res == null || StringUtils.isBlank(res)) {
                // 地区码未找到，请先上报机构信息
                result.put("message", FailReasonEnum.FAIL_22.getDesc());
                return null;
            }
            return res;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", FailReasonEnum.FAIL_00.getDesc());
            return null;
        }
    }

    // 验证可操作性（目前验证机构数据即可，即target = ModuleEnum.ORG_REPORT.getVal()）
    private boolean verifyOperability(String target, JSONObject data, JSONObject result) {
        try {
            int res = 0;
            if (ModuleEnum.ORG_REPORT.getVal().equals(target)) {
                // 机构验证可操作性
                data.put("singleorgid", data.getString(configBean.getParsed_sgid()));
                res = cityReportMapper.verifyOrgOperator(data);
            } else if (ModuleEnum.SYS_REPORT.getVal().equals(target)) {
                // 系统可操作性验证

            } else if (ModuleEnum.CATALOG_REPORT.getVal().equals(target)) {
                // 业务事项可操作性验证

            } else if (ModuleEnum.PUBMATTER_REPORT.getVal().equals(target)) {
                // 公共业务事项可操作性验证

            }
            if (res <= 0) {
                // 不可操作该机构下数据
                result.put("message", FailReasonEnum.FAIL_21.getDesc());
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("code", ResultStatusEnum.FAILURE100.getCode());
            result.put("message", ResultStatusEnum.FAILURE100.getDesc());
            return false;
        }
    }

    //  验证业务系统是否存在，存在则返回业务系统ID，不存在返回空
    private String verifySysIdExist(JSONObject data, JSONObject result) {
        // 判断业务系统是否为空，为空则创建，不为空则判断是否匹配
        String sysname = data.getString("sysname");
        if (null == sysname || StringUtils.isBlank(sysname)) {
            // 判断机构是否匹配，如果机构也不匹配，直接返回失败
            String orgid = verifyOrgIDExist(data.getString("orgname"), null);
            if (null == orgid || StringUtils.isBlank(orgid)) {
                // 机构和业务系统未找到
                result.put("code", ResultStatusEnum.FAILURE101.getCode());
                result.put("message", ResultStatusEnum.FAILURE101.getDesc());
                return null;
            } else {
                // 判断该机构有没有默认的业务系统
                Map<String, String> map = cityReportMapper.querySysDefaultExist(orgid);
                if (map != null && !StringUtils.isBlank(map.get("ID"))) {
                    return map.get("ID");
                } else {
                    // 为该业务事项添加业务系统
                    JSONObject sysData = new JSONObject();
                    String sysid = IdGenerateUtil.getKey();
                    sysData.put("sysid", sysid);
                    sysData.put("orgid", orgid);
                    if (cityReportMapper.addDefaultSysCity(sysData) > 0) {
                        return sysid;
                    } else {
                        result.put("code", ResultStatusEnum.FAILURE100.getCode());
                        result.put("message", FailReasonEnum.FAIL_00.getDesc());
                        return null;
                    }
                }
            }
        } else {
            String sysId = cityReportMapper.verifySysIdExist(data);
            if (sysId == null || StringUtils.isBlank(sysId)) {
                // 机构和业务系统未找到
                result.put("code", ResultStatusEnum.FAILURE102.getCode());
                result.put("message", ResultStatusEnum.FAILURE102.getDesc());
                return null;
            }
            return sysId;
        }
    }

    // 检查业务信息是否存在，存在则返回对应的业务信息ID，不存在则返回空
    private String verifyMatterIDExist(JSONObject data, JSONObject result) {
        String sysname = data.getString("sysname");
        String sysId = "";
        if (null == sysname || StringUtils.isBlank(sysname)) {
            // 查看是否存在默认的业务系统，没有返回错误 业务事项不匹配
            // 判断该机构有没有默认的业务系统
            String orgid = verifyOrgIDExist(data.getString("orgname"), null);
            Map<String, String> map = cityReportMapper.querySysDefaultExist(orgid);
            if (map != null && !StringUtils.isBlank(map.get("ID"))) {
                sysId = map.get("ID");
            } else {
                result.put("code", ResultStatusEnum.FAILURE103.getCode());
                result.put("message", ResultStatusEnum.FAILURE103.getDesc());
                return null;
            }
        } else {
            sysId = cityReportMapper.verifySysIdExist(data);
            if (sysId == null || StringUtils.isBlank(sysId)) {
                // 机构和业务系统未找到
                result.put("code", ResultStatusEnum.FAILURE102.getCode());
                result.put("message", ResultStatusEnum.FAILURE102.getDesc());
                return null;
            }
        }
        data.put("sysid", sysId);
        String matterId = cityReportMapper.verifyMatterExist(data);
        if (matterId == null || StringUtils.isBlank(matterId)) {
            result.put("code", ResultStatusEnum.FAILURE104.getCode());
            result.put("message", ResultStatusEnum.FAILURE104.getDesc());
            return null;
        }
        return matterId;
    }

    // 检查业务事项是否存在，存在则返回对应的业务事项ID，不存在则返回空
    private String verifyCatalogIDExist(JSONObject data, JSONObject result) {
        // 验证业务系统是否存在
        String sysname = data.getString("sysname");
        String sysId = "";
        if (null == sysname || StringUtils.isBlank(sysname)) {
            // 查看是否存在默认的业务系统，没有返回错误 业务事项不匹配
            // 判断该机构有没有默认的业务系统
            String orgid = verifyOrgIDExist(data.getString("orgname"), null);
            Map<String, String> map = cityReportMapper.querySysDefaultExist(orgid);
            if (map != null && !StringUtils.isBlank(map.get("ID"))) {
                sysId = map.get("ID");
            } else {
                result.put("code", ResultStatusEnum.FAILURE103.getCode());
                result.put("message", ResultStatusEnum.FAILURE103.getDesc());
                return null;
            }
        } else {
            sysId = cityReportMapper.verifySysIdExist(data);
            if (sysId == null || StringUtils.isBlank(sysId)) {
                // 机构和业务系统未找到
                result.put("code", ResultStatusEnum.FAILURE102.getCode());
                result.put("message", ResultStatusEnum.FAILURE102.getDesc());
                return null;
            }
        }
        data.put("sysid", sysId);
        String catalogId = cityReportMapper.verifyCatalogExist(data);
        if (catalogId == null || StringUtils.isBlank(catalogId)) {
            result.put("code", ResultStatusEnum.FAILURE103.getCode());
            result.put("message", ResultStatusEnum.FAILURE103.getDesc());
            return null;
        }
        return catalogId;
    }

    // 根据orgname,interfacename检查是否存在对应的机构下的接口，存在则返回对应的接口ID，不存在则返回空
    private String verifyInterfaceIDExist(JSONObject data, JSONObject result) {
        String interfaceId = cityReportMapper.verifyInterfaceIDExist(data);
        if (interfaceId == null || StringUtils.isBlank(interfaceId)) {
            result.put("message", FailReasonEnum.FAIL_26.getDesc());
            return null;
        }
        return interfaceId;
    }

    // 根据orgname检查是否存在对应的机构，存在则返回对应的机构ID，不存在则返回空
    private String verifyOrgIDExist(String orgname, JSONObject result) {
        String orgId = cityReportMapper.getOrgIdByOrgName(orgname);
        if (orgId == null || StringUtils.isBlank(orgId)) {
            if (null != result) {
                result.put("code", ResultStatusEnum.FAILURE101.getCode());
                result.put("message", ResultStatusEnum.FAILURE101.getDesc());
            }
            return null;
        }
        return orgId;
    }

    // 验证操作类型
    private boolean verifyOperator(String operator, JSONObject result) {
        if (OperatorEnum.ADDORUPDATE.getVal().equals(operator) || OperatorEnum.DELETE.getVal().equals(operator)) {
            return true;
        }
        result.put("message", FailReasonEnum.FAIL_01.getDesc() + ":opetype -> addOrUpdate or delete");
        return false;
    }

    // 解析http接口方法输入参数
    private boolean parseHttpParams(JSONObject data, JSONObject result, JSONArray parsedParams, String jsonKey) {
        JSONArray Params = null;
        try {
            Params = data.getJSONArray(jsonKey);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.put("message", FailReasonEnum.FAIL_23.getDesc() + ":" + jsonKey);
            return false;
        }
        if (null == Params || Params.isEmpty()) {
            return true;
        }
        for (int i = 0; i < Params.size(); i++) {
            JSONObject data_i = Params.getJSONObject(i);
            // 验证必填字段
            if ("input_params".equals(jsonKey)) {
                // 输入参数
                if (!httpRequestService.checkNotEmpty(data_i, httpRequestService.needParams("param_name", "param_desc", "param_type"), result)
                    || !httpRequestService.checkNeedParam(data_i, httpRequestService.needParams("param_value"), result)
                )
                    return false;
            } else {
                // 输出参数
                if (!httpRequestService.checkNotEmpty(data_i, httpRequestService.needParams("param_name", "param_desc"), result)
                        || !httpRequestService.checkNeedParam(data_i, httpRequestService.needParams("param_value"), result)
                )
                    return false;
            }
            parsedParams.add(data_i);
        }
        return true;
    }

    // 更新业务信息
    private void updateMatterCity(JSONObject data, JSONObject result) throws Exception {
        int flag = cityReportMapper.updateMatterCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",更新项：" + (flag > 0 ? "1" : "0"));
    }

    // 新增业务信息
    private void addMatterCity(JSONObject data, JSONObject result) throws Exception {
        // 判断该业务信息是否存在
        String query_businessmatter_cname = data.getString("query_businessmatter_cname");
        String businessmatter_cname = data.getString("businessmatter_cname");
        data.put("query_businessmatter_cname", businessmatter_cname);
        if (cityReportMapper.countMatterCity(data) > 0) {
            result.put("code", ResultStatusEnum.FAILURE115.getCode());
            result.put("message", ResultStatusEnum.FAILURE115.getDesc());
            return;
        }
        data.put("query_businessmatter_cname", query_businessmatter_cname);
        data.put("matterid", IdGenerateUtil.getKey());
        int res = cityReportMapper.addMatterCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",新增项：" + (res > 0 ? "1" : "0"));
    }

    // 更新业务事项
    private void updateCatalogCity(JSONObject data, JSONObject result) throws Exception {
        int flag = cityReportMapper.updateCatalogCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",更新项：" + (flag > 0 ? "1" : "0"));
    }

    // 新增业务事项
    private void addCatalogCity(JSONObject data, JSONObject result) throws Exception {
        // 判断该业务事项是否存在
        String query_catalogname = data.getString("query_catalogname");
        String catalogname = data.getString("catalogname");
        data.put("query_catalogname", catalogname);
        if (cityReportMapper.getCatalogCountBySysIDAndCatalogName(data) > 0) {
            result.put("code", ResultStatusEnum.FAILURE114.getCode());
            result.put("message", ResultStatusEnum.FAILURE114.getDesc());
            return;
        }
        data.put("query_catalogname", query_catalogname);
        data.put("catalogid", IdGenerateUtil.getKey());
        int res = cityReportMapper.addCatalogCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",新增项：" + (res > 0 ? "1" : "0"));
    }

    // 更新业务系统
    private void updateSysCity(JSONObject data, JSONObject result) throws Exception {
        int flag = cityReportMapper.updateSysCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",更新项：" + (flag > 0 ? "1" : "0"));
    }

    // 新增业务系统
    private void addSysCity(JSONObject data, JSONObject result) throws Exception {
        // 判断该机构下，该业务系统是否存在
        String query_sysname = data.getString("query_sysname");
        String sysname = data.getString("sysname");
        data.put("query_sysname", sysname);
        if (cityReportMapper.countSysCity(data) > 0) {
            result.put("code", ResultStatusEnum.FAILURE113.getCode());
            result.put("message", ResultStatusEnum.FAILURE113.getDesc());
            return;
        }
        data.put("query_sysname", query_sysname);
        data.put("sysid", IdGenerateUtil.getKey());
        int res = cityReportMapper.addSysCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",新增项：" + (res > 0 ? "1" : "0"));
    }

    // 更新机构
    private void updateOrgCity(JSONObject data, JSONObject result) throws Exception {
        int flag = cityReportMapper.updateOrgCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",更新项：" + (flag > 0 ? "1" : "0"));
    }

    // 新增机构
    private void addOrgCity(JSONObject data, JSONObject result) throws Exception {
        // 判断机构名称是否重复
        if (cityReportMapper.getCountByOrgName(data.getString("orgname")) > 0) {
            // 机构名称已经重复
            result.put("code", ResultStatusEnum.FAILURE112.getCode());
            result.put("message", ResultStatusEnum.FAILURE112.getDesc());
            return;
        }
        data.put("orgId", IdGenerateUtil.getKey());
        data.put("singleorgid", data.getString(configBean.getParsed_sgid()));
        int res = cityReportMapper.addOrgCity(data);
        result.put("code", ResultStatusEnum.SUCCUSS.getCode());
        result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",新增项：" + (res > 0 ? "1" : "0"));
    }

    // 降级
    private void fallBack_orgReport(JSONObject jsonData, JSONObject result) {
        result.put("message", FailReasonEnum.FAIL_17.getDesc());
        logger.info(result.toJSONString());
    }
}
