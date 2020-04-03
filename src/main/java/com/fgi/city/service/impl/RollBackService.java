package com.fgi.city.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.CityReportMapper;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.entity.InterfaceChangeBean;
import com.fgi.city.entity.UserBean;
import com.fgi.city.enums.*;
import com.fgi.city.utils.IdGenerateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RollBackService {
    private transient Logger logger = LoggerFactory.getLogger(RollBackService.class);

    @Autowired
    private CityReportMapper cityReportMapper;
    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    private ConfigBean configBean;

    // 处理指标集合，如果一个指标参数错误则全部回滚
    @Transactional
    public boolean dealWithIndicator(String matterId, JSONArray data, JSONObject result) {
        try {
            for (int i = 0; i < data.size(); i++) {
                JSONObject data_i = data.getJSONObject(i);
                data_i.put("matterid", matterId);
                String opetype = data_i.getString("opetype");
                if (OperatorEnum.ADDORUPDATE.getVal().equals(opetype)) {
                    // 新增获取更新
                    if (cityReportMapper.getIndicatorCountByEnameAndMatterId(data_i) > 0) {
                        // 修改
                        cityReportMapper.updateIndicatorCity(data_i);
                    } else {
                        // 新增
                        // 判断该指标是否已经存在
                        String query_indicator_ename = data_i.getString("query_indicator_ename");
                        String indicator_ename = data_i.getString("indicator_ename");
                        data_i.put("query_indicator_ename", indicator_ename);
                        if (cityReportMapper.getIndicatorCountByEnameAndMatterId(data_i) > 0) {
                            // 该指标名称已经存在
                            result.put("code", ResultStatusEnum.FAILURE116.getCode());
                            result.put("message", ResultStatusEnum.FAILURE116.getDesc());
                            throw new RuntimeException();
                        }
                        data_i.put("query_indicator_ename", query_indicator_ename);
                        data_i.put("indicatorid", IdGenerateUtil.getKey());
                        cityReportMapper.addIndicatorCity(data_i);
                    }
                } else if (OperatorEnum.DELETE.getVal().equals(opetype)) {
                    // 删除
                    if (cityReportMapper.getIndicatorCountByEnameAndMatterId(data_i) <= 0) {
                        // 该指标不存在
                        result.put("code", ResultStatusEnum.FAILURE.getCode());
                        result.put("message", data_i.getString("query_indicator_ename") + "->" + FailReasonEnum.FAIL_44.getDesc());
                        throw new RuntimeException();
                    }
                    cityReportMapper.deleteIndicatorCity(data_i);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 删除接口方法，统一成功，统一失败（添加事务）
    @Transactional
    public void dealMatterDataPreview(JSONArray datapreview, String matterid) {
        try {
            // 删除旧的预览数据
            cityReportMapper.deleteDataPreviewByMatterID(matterid);
            // 添加新数据
            JSONObject param = new JSONObject();
            param.put("datapreview", datapreview.toJSONString());
            param.put("matterid", matterid);
            cityReportMapper.addDataPreviewByMatterID(param);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 添加接口方法（包含变更记录），统一成功，统一失败（添加事务）
    @Transactional
    public void addInterfaceMethod(JSONObject data, JSONArray parsedInput, JSONArray parsedOutParams, JSONObject result) {
        try {
            String batchs = IdGenerateUtil.getKey();
            // 判断该接口方法是否已经存在
            String query_method = data.getString("query_method");
            String method = data.getString("method");
            data.put("query_method", method);
            if (cityReportMapper.getInterfaceMethodCountByInterfaceidAndMethodName(data) > 0) {
                // 该指接口方法名称已经存在
                result.put("code", ResultStatusEnum.FAILURE202.getCode());
                result.put("message", ResultStatusEnum.FAILURE202.getDesc());
                return;
            }
            data.put("query_method", query_method);
            String methodid = IdGenerateUtil.getKey();
            data.put("methodid", methodid);
            cityReportMapper.addInterfaceMethodCity(data);
            // 添加接口变更记录
            Map<String, String> methodOldInfo = new HashMap<>();
            Map<String, String> methodNewInfo = cityReportMapper.queryInterfaceMethodByID(data);
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_METHOD_TABLE.getDesc(), "0", "0", users.getID(), methodid, methodOldInfo, methodNewInfo,
                    "CREATETIME", "UPDATETIME", "MONITORSTATUS", "MONITORDESC", "STATE");
            // 添加输入参数
            logger.info("看下输入参数：" + parsedInput.toJSONString());
            logger.info("看下输出参数：" + parsedOutParams.toJSONString());
            for (int i = 0; i < parsedInput.size(); i++) {
                JSONObject input_i = parsedInput.getJSONObject(i);
                String id = IdGenerateUtil.getKey();
                input_i.put("methodid", methodid);
                input_i.put("fieldid", id);
                input_i.put("sortnum", input_i.getString("param_order"));
                input_i.put("fieldtype", "0");
                cityReportMapper.addInterfaceMethodFieldCity(input_i);
                // 查询旧值
                Map<String, String> fieldOld = new HashMap<>();
                // 查询新值
                Map<String, String> fieldNew = cityReportMapper.queryFieldInfoById(id);
                // 添加参数变更记录， 添加方法时， 只加方法的变更记录 不加底下的参数 2019-10-28 16:32
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "0", users.getID(), id, fieldOld, fieldNew,
                        "UPDATETIME", "CREATETIME", "STATE");
            }
            // 添加输出参数
            for (int i = 0; i < parsedOutParams.size(); i++) {
                JSONObject output_i = parsedOutParams.getJSONObject(i);
                String id = IdGenerateUtil.getKey();
                output_i.put("methodid", methodid);
                output_i.put("fieldid", id);
                output_i.put("sortnum", String.valueOf(i + 1));
                output_i.put("fieldtype", "1");
                cityReportMapper.addInterfaceMethodFieldCity(output_i);
                // 查询旧值
                Map<String, String> fieldOld = new HashMap<>();
                // 查询新值
                Map<String, String> fieldNew = cityReportMapper.queryFieldInfoById(id);
                // 添加参数变更记录  添加方法时， 只加方法的变更记录 不加底下的参数 2019-10-28 16:32
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "0", users.getID(), id, fieldOld, fieldNew,
                        "UPDATETIME", "CREATETIME", "STATE");
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 修改接口方法 （如果该接口已经封装好了，并且修改这个接口方法对调用有影响，这先改为已更新未处理）
    @Transactional
    public void updateInterfaceMethod(JSONObject data, JSONArray inputParams, JSONArray outParams) {
        try {
            String batchs = IdGenerateUtil.getKey();
            // 修改方法
            Map<String, String> methodOldInfo = cityReportMapper.queryMethodInfoByName(data);
            int res = cityReportMapper.updateInterfaceMethodCity(data);
            if (res > 0) {
                // 添加 方法变更记录
                String methodId = methodOldInfo.get("ID");
                data.put("methodid", methodId);
                Map<String, String> methodNewInfo = cityReportMapper.queryInterfaceMethodByID(data);
                UserBean users = (UserBean) data.get(configBean.getParsed_user());
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_METHOD_TABLE.getDesc(), "0", "2", users.getID(), methodId, methodOldInfo, methodNewInfo,
                        "CREATETIME", "UPDATETIME", "MONITORSTATUS", "MONITORDESC", "STATE");
                int impactResult = impactInterfaceCity(1, methodNewInfo, methodOldInfo, methodOldInfo.get("PUBLISHSTATE"));
                // id,methodid,fieldtype,sortnum,paramname,paramtype,describe,PARAMVALUE
                // fieldid,methodid,fieldtype,sortnum,param_name,param_type,param_describe,param_value
                // 输入操作（包含变更记录、是否影响现有接口发布状态）
                logger.info("看下输入参数：" + inputParams.toJSONString());
                logger.info("看下输出参数：" + outParams.toJSONString());
                List<Map<String, String>> FieldInOldInfo = cityReportMapper.queryIOFieldByMethodId(methodId, "0");
                methodFieldOperation(batchs, methodId, users.getID(), FieldInOldInfo, inputParams, 0, 0, impactResult, methodOldInfo.get("PUBLISHSTATE"));
                // 输出操作（包含变更记录、是否影响现有接口发布状态）
                List<Map<String, String>> FieldOutOldInfo = cityReportMapper.queryIOFieldByMethodId(methodId, "1");
                methodFieldOperation(batchs, methodId, users.getID(), FieldOutOldInfo, outParams, 0, 1, impactResult, methodOldInfo.get("PUBLISHSTATE"));
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * 编辑方法参数，因http、wsdl的方法参数不一致，所以这边用状态区分 type=0表示输入参数 type=1表示输出参数
     * methodType=0表示wsdl methodType=1表示http
     *
     * @param methodId
     * @param userId
     * @param FieldOldInfo
     * @param Params
     * @param impactResult
     */
    public void methodFieldOperation(String batchs, String methodId, String userId, List<Map<String, String>> FieldOldInfo, JSONArray Params, int methodType, int type, int impactResult, String methodState) {
        // inputParams (新上报)
        for (int i = 0; i < Params.size(); i++) {
            JSONObject input = Params.getJSONObject(i);
            String param_name = input.getString("param_name");
            boolean flag = false;
            // 原来的
            if (FieldOldInfo != null) {
                for (Map<String, String> map : FieldOldInfo) {
                    String param_temp = map.get("PARAMNAME");
                    if (param_name.equals(param_temp)) {
                        flag = true;
                        break;
                    }
                }
            }
            input.put("methodid", methodId);
            if (type == 0) {
                // 输入
                input.put("fieldtype", "0");
            } else if (type == 1) {
                // 输出
                input.put("fieldtype", "1");
            }
            if (flag) {
                // 修改，根据param_name做修改    param_describe
                input.put("sortnum", input.getString("param_order"));
                if (methodType == 1) {
                    // 1表示http
                    // 如果是http param_desc , 如果是wsdl param_describe、param_order
                    input.put("param_describe", input.getString("param_desc"));
                }
                // 查询旧值
                Map<String, String> fieldOld = cityReportMapper.queryMethodFieldByName(input);
                String fieldId = fieldOld.get("ID");
                cityReportMapper.updateInterfaceMethodFieldCity(input);
                // 查询新值
                Map<String, String> fieldNew = cityReportMapper.queryMethodFieldByName(input);
                // 添加参数变更记录
                if (type == 0) {
                    addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "2", userId, fieldId, fieldOld, fieldNew,
                            "UPDATETIME", "CREATETIME", "STATE");
                } else if (type == 1) {
                    // 输出去除paramtype的比较（输出参数没有这个字段）
                    addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "2", userId, fieldId, fieldOld, fieldNew,
                            "UPDATETIME", "CREATETIME", "STATE", "PARAMTYPE");
                }
                // 判断此次修改是否影响接口调用
                if (impactResult == -1) {
                    impactInterfaceCity(2, fieldOld, fieldNew, methodState);
                }
            } else {
                // 新增，新增param_name
                String id = IdGenerateUtil.getKey();
                input.put("fieldtype", "0");
                input.put("sortnum", input.getString("param_order"));
                input.put("fieldid", id);
                if (methodType == 1) {
                    // 1表示http
                    // 如果是http param_desc , 如果是wsdl param_describe、param_order
                    input.put("param_describe", input.getString("param_desc"));
                }
                Map<String, String> fieldOld = new HashMap<>();
                cityReportMapper.addInterfaceMethodFieldCity(input);
                // 查询新值
                Map<String, String> fieldNew = cityReportMapper.queryFieldInfoById(id);
                // 添加参数变更记录
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "0", userId, id, fieldOld, fieldNew,
                        "UPDATETIME", "CREATETIME", "STATE");
                // 判断此次修改是否影响接口调用 修改方法发布状态为以更新为处理
                if (impactResult == -1 && !InterfacePublishStateEnum.NOPUBLISH.getCode().equals(methodState)) {
                    // 有影响
                    JSONObject params = new JSONObject();
                    params.put("publishstate", InterfacePublishStateEnum.UPDATENODEAL.getCode());
                    params.put("methodid", methodId);
                    interfaceMapper.updateMethodState(params);
                }
            }
        }
        if (FieldOldInfo != null) {
            for (Map<String, String> map : FieldOldInfo) {
                String param_temp = map.get("PARAMNAME");
                String id = map.get("ID");
                boolean ftip = false;
                for (int i = 0; i < Params.size(); i++) {
                    JSONObject input = Params.getJSONObject(i);
                    String param_name = input.getString("param_name");
                    if (param_temp.equals(param_name)) {
                        ftip = true;
                        break;
                    }
                }
                if (!ftip) {
                    // 删除，根据param_name做删除
                    Map<String, String> fieldOld = cityReportMapper.queryFieldInfoById(id);
                    cityReportMapper.deleteMethodFieldById(id);
                    Map<String, String> fieldNew = new HashMap<>();
                    // 添加参数变更记录
                    addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "1", userId, id, fieldOld, fieldNew,
                            "UPDATETIME", "CREATETIME", "STATE");
                    // 判断此次修改是否影响接口调用 修改方法发布状态为以更新为处理
                    if (impactResult == -1) {
                        if (!InterfacePublishStateEnum.NOPUBLISH.getCode().equals(methodState)) {
                            // 有影响
                            JSONObject params = new JSONObject();
                            params.put("publishstate", InterfacePublishStateEnum.UPDATENODEAL.getCode());
                            params.put("methodid", methodId);
                            interfaceMapper.updateMethodState(params);
                        }
                    }
                }
            }
        }
    }

    // 删除接口方法（如果该接口已经封装好了，则不能直接删除接口，先改为已删除未处理）
    @Transactional
    public void deleteInterfaceMethod(JSONObject data, JSONObject result) {
        try {
            String batchs = IdGenerateUtil.getKey();
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            String methodId = cityReportMapper.getInterfaceMethodIDByInterfaceIDAndMethodName(data);
            data.put("methodid", methodId);
            // 查询接口方法
            Map<String, String> methodOldInfo = cityReportMapper.queryInterfaceMethodByID(data);
            boolean rest = deleteInterfaceImpact(1, methodId, methodOldInfo.get("PUBLISHSTATE"));
            Map<String, String> fieldNewInfo = new HashMap<>();
            if (!rest) {
                // 没有影响
                // 删除方法参数
                cityReportMapper.deleteInterfaceMethodFieldCity(data);
                // 删除方法
                cityReportMapper.deleteInterfaceMethodCity(data);
            }
            // 添加接口方法删除变更记录
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_METHOD_TABLE.getDesc(), "0", "1", users.getID(), methodOldInfo.get("ID"), methodOldInfo, fieldNewInfo);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除接口如果对封装有影响，将接口发布状态修改为已删除未处理
     *
     * @param type         类型 0:表示接口，1:表示方法
     * @param interfaceId  接口ID
     * @param publishstate 接口发布状态
     */
    public boolean deleteInterfaceImpact(int type, String interfaceId, String publishstate) {
        boolean res = false;
        if (!InterfacePublishStateEnum.NOPUBLISH.getCode().equals(publishstate)) {
            // 如果不是待发布状态下被删除，都要修改为接口已删除未处理
            if (0 == type) {
                // 接口，修改该接口下所有方法的发布状态
                interfaceMapper.updateMethodStateByInterfaceId(interfaceId, InterfacePublishStateEnum.DELETENODEAL.getCode());
                // 修改接口状态
                interfaceMapper.updateInterfaceState(interfaceId, InterfacePublishStateEnum.DELETENODEAL.getCode());
            } else if (1 == type) {
                // 方法，修改方法的发布状态
                JSONObject params = new JSONObject();
                params.put("publishstate", InterfacePublishStateEnum.DELETENODEAL.getCode());
                params.put("methodid", interfaceId);
                interfaceMapper.updateMethodState(params);
            }
            res = true;
        }
        return res;
    }

    // 删除地市接口信息 （如果接口已经封装好了，先改为已删除未处理状态）
    @Transactional
    public void deleteInterfaceCity(JSONObject data, JSONObject result) throws Exception {
        try {
            Map<String, String> interfaceOldInfo = cityReportMapper.queryInterfaceInfoByName(data);
            // 判断接口删除是否对当前接口调用有影响，如果有影响则修改接口发布状态，否则直接删除, 暂时注释 二阶段在开启
            boolean rest = deleteInterfaceImpact(0, interfaceOldInfo.get("ID"), interfaceOldInfo.get("PUBLISHSTATE"));
            // 没有影响 返回false, 有影响返回true
            String batchs = IdGenerateUtil.getKey();
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            Map<String, String> interfaceNewInfo = new HashMap<>();
            if (!rest) {
                // 获取该接口下所有方法
                List<Map<String, String>> methodList = cityReportMapper.getInterfaceMethodByInterface(data);
                if (methodList != null) {
                    for (Map<String, String> map : methodList) {
                        String methodId = map.get("ID");
                        data.put("methodid", methodId);
                        // 删除该方法的参数
                        cityReportMapper.deleteInterfaceMethodFieldCity(data);
                        // 删除该方法
                        cityReportMapper.deleteInterfaceMethodByMethodID(data);
                    }
                }
                // 删除接口
                cityReportMapper.deleteInterfaceCity(data);
            }
            // 添加接口删除变更记录
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_TABLE.getDesc(), "0", "1", users.getID(), interfaceOldInfo.get("ID"), interfaceOldInfo, interfaceNewInfo);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 删除业务信息
    @Transactional
    public void deleteMatterCity(JSONObject data, JSONObject result) throws Exception {
        try {
            // 如果这个业务信息属于私有事项，删除业务信息之前要删除指标项
            Map<String, String> matterInfo = cityReportMapper.getMatterInfoByMatterNameAndCatalogID(data);
            if (matterInfo != null) {
                String matterId = matterInfo.get("ID");
                data.put("methodid", matterId);
                // 关联的私有事项，需要删除对应的指标项
                cityReportMapper.deleteIndicatorByMatterID(data);
            }
            int count = cityReportMapper.deleteMatterCity(data);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",删除项：" + (count > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 删除业务事项
    @Transactional
    public void deleteCatalogCity(JSONObject data, JSONObject result) throws Exception {
        try {
            // 删除业务事项下的业务信息
            List<Map<String, String>> matterList = cityReportMapper.getMattersByCatalogName(data);
            if (matterList != null) {
                deleteMatterService(matterList, data);
            }
            // 删除业务事项
            int count = cityReportMapper.deleteCatalogCity(data);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",删除项：" + (count > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 删除业务系统
    @Transactional
    public void deleteSysCity(JSONObject data, JSONObject result) throws Exception {
        try {
            // 查询该系统的所有业务事项
            List<Map<String, String>> catalogList = cityReportMapper.getCatalogBySysName(data);
            if (catalogList != null) {
                deleteCatalogService(catalogList, data);
            }
            // 删除业务系统
            int count = cityReportMapper.deleteSysCity(data);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",删除项：" + (count > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 删除机构
    @Transactional
    public void deleteOrgCity(JSONObject data, JSONObject result) throws Exception {
        try {
            // 删除该机构下所有接口信息
            List<Map<String, String>> interfaceList = cityReportMapper.getInterfaceByOrgID(data);
            for (Map<String, String> map : interfaceList) {
                String interfaceId = map.get("ID");
                // 删除该接口下所有方法和参数
                data.put("interfaceid", interfaceId);
                List<Map<String, String>> methodList = cityReportMapper.getInterfaceMethodByInterfaceID(data);
                for (Map<String, String> method : methodList) {
                    String methodId = method.get("ID");
                    data.put("methodid", methodId);
                    // 删除参数和方法
                    int res = cityReportMapper.deleteInterfaceMethodFieldCity(data);
                    cityReportMapper.deleteInterfaceMethodByMethodID(data);
                }
                // 删除该接口
                cityReportMapper.deleteInterfaceByInterfaceID(data);
            }
            // 删除机构下的所有业务系统
            List<Map<String, String>> sysList = cityReportMapper.getSysByOrgname(data);
            for (Map<String, String> sys : sysList) {
                String sysId = sys.get("ID");
                // 获取该系统的所有业务事项
                data.put("sysid", sysId);
                List<Map<String, String>> catalogList = cityReportMapper.getCatalogsBySysID(data);
                deleteCatalogService(catalogList, data);
                // 删除对应业务系统
                cityReportMapper.deleteSysBySysID(data);
            }
            // 删除机构
            int count = cityReportMapper.deleteOrgCity(data);
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",删除项：" + (count > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 删除多个业务信息业务逻辑
    public void deleteMatterService(List<Map<String, String>> matterList, JSONObject data) {
        for (Map<String, String> matter : matterList) {
            String matterId = matter.get("ID");
            data.put("matterid", matterId);
            // 关联的私有事项，需要删除对应的指标项
            cityReportMapper.deleteIndicatorByMatterID(data);
            // 删除对应业务信息
            cityReportMapper.deleteMatterByMatterID(data);
        }
    }

    // 删除多个业务事项业务逻辑
    public void deleteCatalogService(List<Map<String, String>> catalogList, JSONObject data) {
        for (Map<String, String> catalog : catalogList) {
            // 获取业务事项下所有的业务信息
            data.put("catalogid", catalog.get("ID"));
            List<Map<String, String>> matterList = cityReportMapper.getMattersByCatalogID(data);
            deleteMatterService(matterList, data);
            // 删除对应业务事项
            cityReportMapper.deleteCatalogByCatalogID(data);
        }
    }

    // 新增地市接口信息(http)
    @Transactional
    public void addHttpInterfaceCity(JSONObject data, JSONArray inputParam, JSONArray outParams, JSONObject result) throws Exception {
        try {
            String batchs = IdGenerateUtil.getKey();
            // 判断该接口名称是否存在
            String query_interface_name = data.getString("query_interface_name");
            String interface_name = data.getString("interface_name");
            data.put("query_interface_name", interface_name);
            if (cityReportMapper.getInterfaceCountByOrgIDAndName(data) > 0) {
                // 该指接口方法名称已经存在
                result.put("code", ResultStatusEnum.FAILURE201.getCode());
                result.put("message", ResultStatusEnum.FAILURE201.getDesc());
                return;
            }
            data.put("query_interface_name", query_interface_name);
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            String interfaceId = IdGenerateUtil.getKey();
            data.put("interfaceid", interfaceId);
            // 添加接口名称信息
            Map<String, String> interfaceOldInfo = new HashMap<>();

            data.put("sender_id", StringUtils.isBlank(data.getString("sender_id")) ? "" : data.getString("sender_id"));
            data.put("authorized_id", StringUtils.isBlank(data.getString("authorized_id")) ? "" : data.getString("authorized_id"));
            data.put("service_id", StringUtils.isBlank(data.getString("service_id")) ? "" : data.getString("service_id"));

            int count = cityReportMapper.addInterfaceCity(data);
            Map<String, String> interfaceNewInfo = cityReportMapper.getInterfaceInfoById(interfaceId);
            // 添加接口变更记录
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_TABLE.getDesc(), "0", "0", users.getID(), interfaceId, interfaceOldInfo, interfaceNewInfo, "CREATETIME", "UPDATETIME");

            // 添加请求类型、请求参数格式等消息
            String methodId = IdGenerateUtil.getKey();
            data.put("methodid", methodId);
            // 添加接口具体信息
            Map<String, String> methodOldInfo = new HashMap<>();
            cityReportMapper.addHttpInterfaceMethodCity(data);
            Map<String, String> methodNewInfo = cityReportMapper.queryInterfaceMethodByID(data);
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_METHOD_TABLE.getDesc(), "0", "0", users.getID(), methodId, methodOldInfo, methodNewInfo,
                    "CREATETIME", "UPDATETIME", "MONITORSTATUS", "MONITORDESC", "STATE");

            // 添加输入参数
            addInAndOutParams(batchs, inputParam, outParams, methodId, users.getID());
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",添加项:" + (count > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 修改地市接口信息(http) 如果该接口已经封装好了，并且修改这个接口方法对调用有影响，这先改为已更新未处理
    @Transactional
    public void updateHttpInterfaceCity(JSONObject data, JSONArray inputParam, JSONArray outParams, JSONObject result) throws Exception {
        try {
            String batchs = IdGenerateUtil.getKey();
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            Map<String, String> interfaceOldInfo = cityReportMapper.queryInterfaceInfoByName(data);
            // 修改接口名称信息
            int count = cityReportMapper.updateInterfaceCity(data);
            if (count > 0) {
                String interfaceId = interfaceOldInfo.get("ID");
                Map<String, String> interfaceNewInfo = cityReportMapper.getInterfaceInfoById(interfaceId);
                // 添加接口变更记录
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_TABLE.getDesc(), "0", "2", users.getID(), interfaceId, interfaceOldInfo, interfaceNewInfo, "CREATETIME", "UPDATETIME");
                // 先判断接口修改是否对接口调用有影响， 一阶段没有封装只会返回-1
                int impactInterface = impactInterfaceCity(0, interfaceNewInfo, interfaceOldInfo, interfaceOldInfo.get("PUBLISHSTATE"));
                data.put("interfaceid", interfaceId);
                String methodId = cityReportMapper.getHttpIDByInterfaceID(data);
                data.put("methodid", methodId);
                Map<String, String> methodOldInfo = cityReportMapper.queryInterfaceMethodByID(data);
                // 修改接口具体信息
                int resMethod = cityReportMapper.updateHttpInterfaceMethodCity(data);
                if (resMethod > 0) {
                    Map<String, String> methodNewInfo = cityReportMapper.queryInterfaceMethodByID(data);
                    addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_METHOD_TABLE.getDesc(), "0", "2", users.getID(), methodId, methodOldInfo, methodNewInfo,
                            "CREATETIME", "UPDATETIME", "MONITORSTATUS", "MONITORDESC", "STATE");
                    int impact = 1;
                    if (impactInterface == -1) {
                        // 接口没有影响
                        impact = impactInterfaceCity(1, methodNewInfo, methodOldInfo, methodOldInfo.get("PUBLISHSTATE"));
                    }
                    // 输入操作（包含变更记录）
                    List<Map<String, String>> FieldInOldInfo = cityReportMapper.queryIOFieldByMethodId(methodId, "0");
                    methodFieldOperation(batchs, methodId, users.getID(), FieldInOldInfo, inputParam, 1, 0, impact, methodOldInfo.get("PUBLISHSTATE"));
                    // 输出操作（包含变更记录）
                    List<Map<String, String>> FieldOutOldInfo = cityReportMapper.queryIOFieldByMethodId(methodId, "1");
                    methodFieldOperation(batchs, methodId, users.getID(), FieldOutOldInfo, outParams, 1, 1, impact, methodOldInfo.get("PUBLISHSTATE"));
                }
            }
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",更新项:" + (count > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 添加输入输出参数
    public void addInAndOutParams(String batchs, JSONArray inputParam, JSONArray outParams, String methodId, String userId) {
        // 添加输入参数
        for (int i = 0; i < inputParam.size(); i++) {
            String id = IdGenerateUtil.getKey();
            JSONObject data_i = inputParam.getJSONObject(i);
            data_i.put("fieldtype", "0");
            data_i.put("fieldid", id);
            data_i.put("methodid", methodId);
            data_i.put("sortnum", String.valueOf(i + 1));
//            data_i.put("param_type", "");
            data_i.put("param_describe", data_i.getString("param_desc"));
            cityReportMapper.addInterfaceMethodFieldCity(data_i);
            // 查询旧值
            Map<String, String> fieldOld = new HashMap<>();
            // 查询新值
            Map<String, String> fieldNew = cityReportMapper.queryFieldInfoById(id);
            // 添加参数变更记录
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "0", userId, id, fieldOld, fieldNew,
                    "UPDATETIME", "PARAMVALUE", "CREATETIME", "STATE");
        }
        // 添加输出参数
        for (int j = 0; j < outParams.size(); j++) {
            String id = IdGenerateUtil.getKey();
            JSONObject data_j = outParams.getJSONObject(j);
            data_j.put("fieldtype", "1");
            data_j.put("fieldid", id);
            data_j.put("methodid", methodId);
            data_j.put("sortnum", String.valueOf(j + 1));
            data_j.put("param_type", "");
            data_j.put("param_describe", data_j.getString("param_desc"));
            cityReportMapper.addInterfaceMethodFieldCity(data_j);
            // 查询旧值
            Map<String, String> fieldOld = new HashMap<>();
            // 查询新值
            Map<String, String> fieldNew = cityReportMapper.queryFieldInfoById(id);
            // 添加参数变更记录
            addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc(), "0", "0", userId, id, fieldOld, fieldNew,
                    "UPDATETIME", "PARAMVALUE", "CREATETIME", "STATE");
        }
    }

    // 保存地市账号密码，先删后加
    @Transactional
    public void saveAccountAndPwdCity(JSONObject data) {
        try {
            data.put("id", IdGenerateUtil.getKey());
            cityReportMapper.deleteAccountAndPwdCity(data);
            cityReportMapper.addAccountAndPwdCity(data);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 添加事务，要么全部成功，要么全部失败
    @Transactional
    public void saveReportLog(JSONObject jsonData, Map<String, String> interfaceInfo) {
        try {
            JSONArray jsonArray = jsonData.getJSONArray("details");
            if (jsonArray != null && jsonArray.size() > 0) {
                // 解析JSONArray
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject json_i = jsonArray.getJSONObject(i);
                    json_i.put("id", IdGenerateUtil.getKey());
                    json_i.put("interfaceid", interfaceInfo.get("interfaceid"));
                    json_i.put("methodid", interfaceInfo.get("methodid"));
                    json_i.put("interfacename", interfaceInfo.get("interfacename"));
                    json_i.put("methodname", interfaceInfo.get("methodname"));
                    json_i.put("methodename", interfaceInfo.get("methodename"));
                    json_i.put("interfacetype", interfaceInfo.get("interfacetype"));
                    cityReportMapper.addInterfaceLogCity(json_i);
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 添加变更记录 type 变更类型，0：新增、1：删除、2：修改  ,  flag 是否删除数据，0：有效，1：无效
    public void addChangeRecord(String batchs, String changeTable, String flag, String type, String userId, String recordId, Map<String, String> oldMap, Map<String, String> newMap, String... ignore) {
        // 判断修改的项
        Map tempMap = new HashMap();
        if (ignore != null) {
            for (String s : ignore) {
                tempMap.put(s, "");
            }
        }
        if ("0".equals(type)) {
            for (Map.Entry<String, String> entry : newMap.entrySet()) {
                String key = entry.getKey();
                if (tempMap.containsKey(key)) {
                    // 这两个参数不做对比，时间对比没有意义
                    continue;
                }
                // 添加时改为添加一条变更记录 暂且用ID字段作为判断依据 修改时间2019-10-28 15:04
                if ("ID".equals(key)) {
                    Object value = entry.getValue() == null ? "" : entry.getValue();
                    String oldVal = "";
                    // 改为保存中文名称，id暂时用不上，修改时间2019/11/11
//                    String newVal = String.valueOf(value);
                    String cname = getParamCname(changeTable, newMap);
                    InterfaceChangeBean change = new InterfaceChangeBean();
                    change.setId(IdGenerateUtil.getKey());
                    change.setChangetableid(recordId);
                    change.setChangetable(changeTable);
                    change.setChangecolumn(key);
                    change.setOldvalue(oldVal); // 原值
                    change.setNewvalue(cname); // 新值
                    change.setType(type); // 变更类型，0：新增、1：删除、2：修改
                    change.setCreatorid(userId); // 创建人id
                    change.setFlag(flag); // 是否删除数据，0：有效，1：无效
                    change.setBatch(batchs);
                    // 添加变更记录
                    cityReportMapper.addInterfaceChangeCity(change);
                }
            }
        } else {
            // 编辑
            if ("2".equals(type)) {
                for (Map.Entry<String, String> entry : oldMap.entrySet()) {
                    String key = entry.getKey();
                    if (tempMap.containsKey(key)) {
                        // 这两个参数不做对比，时间对比没有意义
                        continue;
                    }
                    Object objVal = entry.getValue() == null ? "" : entry.getValue();
                    String oldVal = String.valueOf(objVal);
                    String newVal = "";
                    if (newMap.containsKey(key)) {
                        Object tempObj = newMap.get(key) == null ? "" : newMap.get(key);
                        newVal = String.valueOf(tempObj);
                    }
                    if (!oldVal.equals(newVal)) {
                        // 发生变更 添加该项记录变更
                        InterfaceChangeBean change = new InterfaceChangeBean();
                        change.setId(IdGenerateUtil.getKey());
                        change.setChangetableid(recordId);
                        change.setChangetable(changeTable);
                        change.setChangecolumn(key);
                        change.setOldvalue(oldVal); // 原值
                        change.setNewvalue(newVal); // 新值
                        change.setType(type); // 变更类型，0：新增、1：删除、2：修改
                        change.setCreatorid(userId); // 创建人id
                        change.setFlag(flag); // 是否删除数据，0：有效，1：无效
                        change.setBatch(batchs);
                        // 添加变更记录
                        cityReportMapper.addInterfaceChangeCity(change);
                    }
                }
            } else if ("1".equals(type)) {
                // 删除
                for (Map.Entry<String, String> entry : oldMap.entrySet()) {
                    String key = entry.getKey();
                    if (tempMap.containsKey(key)) {
                        // 这两个参数不做对比，时间对比没有意义
                        continue;
                    }
                    // 删除时改为添加一条变更记录 暂且用ID字段作为判断依据 修改时间2019-10-28 15:04
                    if ("ID".equals(key)) {
                        // 改为保存中文名称，id暂时用不上，修改时间2019/11/11
//                        String oldVal = String.valueOf(entry.getValue() == null ? "" : entry.getValue());
                        String newVal = "";
                        String cname = getParamCname(changeTable, oldMap);
                        InterfaceChangeBean change = new InterfaceChangeBean();
                        change.setId(IdGenerateUtil.getKey());
                        change.setChangetableid(recordId);
                        change.setChangetable(changeTable);
                        change.setChangecolumn(key);
                        change.setOldvalue(cname); // 原值
                        change.setNewvalue(newVal); // 新值
                        change.setType(type); // 变更类型，0：新增、1：删除、2：修改
                        change.setCreatorid(userId); // 创建人id
                        change.setFlag(flag); // 是否删除数据，0：有效，1：无效
                        change.setBatch(batchs);
                        // 添加变更记录
                        cityReportMapper.addInterfaceChangeCity(change);
                    }
                }
            }
        }
    }

    // 获取参数中文名称
    public String getParamCname(String changeTable, Map<String, String> paramMap) {
        String cname = "";
        if (InterfaceChangeTableEnum.INTERFACE_PARAM_TABLE.getDesc().equals(changeTable)) {
            // 参数
            cname = paramMap.get("PARAMNAME");
        } else if (InterfaceChangeTableEnum.INTERFACE_METHOD_TABLE.getDesc().equals(changeTable)) {
            // 方法
            cname = paramMap.get("METHODNAME");
        } else if (InterfaceChangeTableEnum.INTERFACE_TABLE.getDesc().equals(changeTable)) {
            // 接口
            cname = paramMap.get("INTERFACENAME");
        }
        return cname;
    }

    /**
     * 判断接口修改是否对现有的接口封装有影响
     * (1)接口影响修改接口发布状态、方法发布状态
     * (2)方法影响修改方法发布状态
     * (3)参数影响修改方法发布状态
     *
     * @param type             类型，0->接口，1->方法，2->参数
     * @param interfaceNewInfo 接口新值
     * @param interfaceOldInfo 接口旧值
     * @param publishstate     修改前的发布状态
     * @return -1没有影响， 1影响
     */
    public int impactInterfaceCity(int type, Map<String, String> interfaceNewInfo, Map<String, String> interfaceOldInfo, String publishstate) {
        // 判断接口当前状态，如果不是‘未发布状态’则要判断此次的修改是否对接口有影响
        if (!InterfacePublishStateEnum.NOPUBLISH.getCode().equals(publishstate)) {
            // 比较新旧值
            if (interfaceOldInfo != null) {
                String interfaceId = interfaceOldInfo.get("ID");
                for (Map.Entry<String, String> entry : interfaceOldInfo.entrySet()) {
                    String keyStr = entry.getKey();
                    String oldValTemp = String.valueOf(interfaceOldInfo.get(keyStr));
                    String newValTemp = String.valueOf(interfaceNewInfo.get(keyStr));
                    String oldVal = oldValTemp == null ? "" : oldValTemp;
                    String newVal = newValTemp == null ? "" : newValTemp;
                    if (!oldVal.equals(newVal)) {
                        if (0 == type) {
                            // 接口
                            if (!InterfaceImpactEnum.containsKey(keyStr)) {
                                // 说明有影响 修改接口状态为：已更新未处理
                                interfaceMapper.updateInterfaceState(interfaceId, InterfacePublishStateEnum.UPDATENODEAL.getCode());
                                // 修改该接口底下所有方法的状态为：已更新未处理
                                interfaceMapper.updateMethodStateByInterfaceId(interfaceId, InterfacePublishStateEnum.UPDATENODEAL.getCode());
                                // 修改接口方法的监测状态
                                updateMonitorStatusByInterface(interfaceId);
                                return 1;
                            }
                        } else if (1 == type) {
                            // 方法
                            if (!MethodImpactEnum.containsKey(keyStr)) {
//                                String interfaceIdTemp = interfaceOldInfo.get("INTERFACEID");
                                // 说明有影响 修改接口方法状态为：已更新未处理
                                JSONObject params = new JSONObject();
                                params.put("publishstate", InterfacePublishStateEnum.UPDATENODEAL.getCode());
                                params.put("methodid", interfaceId);
                                interfaceMapper.updateMethodState(params);
                                // 修改接口方法的监测状态
                                updateMonitorStatus(interfaceId);

                                // 如果有其他方法的状态是正常的，则不改变整个接口状态，暂时不改
                                // 如果没有其他方法的状态是正常的，则改变整个接口状态，暂时不改
                                // 如果有方法被修改了，先将整个接口的状态修改为已更新未处理，暂时不改
//                                interfaceMapper.updateInterfaceState(interfaceIdTemp, InterfacePublishStateEnum.UPDATENODEAL.getCode());
                                return 1;
                            }
                        } else if (2 == type) {
                            // 参数
                            if (!FieldImpactEnum.containsKey(keyStr)) {
                                // 说明有影响 修该该参数对应的方法状态为：已更新未处理
                                String methodIdTemp = interfaceOldInfo.get("METHODID");
//                                Map<String, String> methodMap = interfaceMapper.queryMethodByID(methodIdTemp);
//                                String interfaceIdTemp = methodMap.get("INTERFACEID");
                                JSONObject params = new JSONObject();
                                params.put("publishstate", InterfacePublishStateEnum.UPDATENODEAL.getCode());
                                params.put("methodid", methodIdTemp);
                                interfaceMapper.updateMethodState(params);
                                // 修改接口方法的监测状态
                                updateMonitorStatus(methodIdTemp);
                                return 1;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    // 重置接口方法监测状态
    public void updateMonitorStatus(String interfaceId) {
        JSONObject paramJson = new JSONObject();
        paramJson.put("code", "0");
        paramJson.put("message", "");
        paramJson.put("id", interfaceId);
        interfaceMapper.updateInterfaceMonitorStatus(paramJson);
    }

    // 重置接口方法监测状态
    public void updateMonitorStatusByInterface(String interfaceId) {
        JSONObject paramJson = new JSONObject();
        paramJson.put("code", "0");
        paramJson.put("message", "");
        paramJson.put("id", interfaceId);
        interfaceMapper.updateMonitorStatusByInterface(paramJson);
    }

    // 更新地市接口信息(webservice) 包含变更记录 （如果该接口已经封装出去了，并且修改这个接口方法对调用有影响，这先改为已更新未处理）
    @Transactional
    public void updateInterfaceCity(JSONObject data, JSONObject result) {
        try {
            // 生成同一个批次上报标示
            String batchs = IdGenerateUtil.getKey();
            // 查询接口ID
            String interface_id = cityReportMapper.getInterfaceIdByOrgIDAndInterfaceName(data);
            // 查询接口旧值
            Map<String, String> interfaceOldInfo = cityReportMapper.getInterfaceInfoById(interface_id);
            // 更新
            int flag = cityReportMapper.updateInterfaceCity(data);
            if (flag > 0) {
                // 更新成功，添加变更记录
                // 查询接口新值
                Map<String, String> interfaceNewInfo = cityReportMapper.getInterfaceInfoById(interface_id);
                UserBean users = (UserBean) data.get(configBean.getParsed_user());
                // 添加变更记录
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_TABLE.getDesc(), "0", "2", users.getID(), interface_id, interfaceOldInfo, interfaceNewInfo, "CREATETIME", "UPDATETIME");
                // 判断是否需要改变接口状态，如果此次修改的接口参数影响到了接口调用，则修改接口状态为已更新未处理，暂时先注释 第二阶段在打开
                impactInterfaceCity(0, interfaceNewInfo, interfaceOldInfo, interfaceOldInfo.get("PUBLISHSTATE"));
            }
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",更新项：" + (flag > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    // 新增地市接口信息(webservice)
    @Transactional
    public void addInterfaceCity(JSONObject data, JSONObject result) {
        try {
            String batchs = IdGenerateUtil.getKey();
            UserBean users = (UserBean) data.get(configBean.getParsed_user());
            // 查询该接口是否已经存在
            String query_interface_name = data.getString("query_interface_name");
            String interface_name = data.getString("interface_name");
            data.put("query_interface_name", interface_name);
            if (cityReportMapper.getInterfaceCountByOrgIDAndName(data) > 0) {
                result.put("code", ResultStatusEnum.FAILURE201.getCode());
                result.put("message", ResultStatusEnum.FAILURE201.getDesc());
                return;
            }
            data.put("query_interface_name", query_interface_name);
            String interfaceId = IdGenerateUtil.getKey();
            data.put("interfaceid", interfaceId);
            // 添加接口名称信息

            data.put("sender_id", StringUtils.isBlank(data.getString("sender_id")) ? "" : data.getString("sender_id"));
            data.put("authorized_id", StringUtils.isBlank(data.getString("authorized_id")) ? "" : data.getString("authorized_id"));
            data.put("service_id", StringUtils.isBlank(data.getString("service_id")) ? "" : data.getString("service_id"));

            int res = cityReportMapper.addInterfaceCity(data);
            if (res > 0) {
                // 新增成功，添加变更记录
                Map<String, String> interfaceOldInfo = new HashMap<>();
                Map<String, String> interfaceNewInfo = cityReportMapper.getInterfaceInfoById(interfaceId);
                // 添加接口变更记录
                addChangeRecord(batchs, InterfaceChangeTableEnum.INTERFACE_TABLE.getDesc(), "0", "0", users.getID(), interfaceId, interfaceOldInfo, interfaceNewInfo, "CREATETIME", "UPDATETIME");
            }
            result.put("code", ResultStatusEnum.SUCCUSS.getCode());
            result.put("message", ResultStatusEnum.SUCCUSS.getDesc() + ",新增项：" + (res > 0 ? "1" : "0"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}