package com.fgi.city.task;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.LogMapper;
import com.fgi.city.entity.LogBean;
import com.fgi.city.entity.UserBean;
import com.fgi.city.enums.OperatorEnum;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.utils.IdGenerateUtil;
import org.apache.commons.lang.StringUtils;

public class LogTask implements Runnable {
    private ConfigBean configBean = BeanProvider.getBean(ConfigBean.class);
    private LogMapper logMapper = BeanProvider.getBean(LogMapper.class);
    private JSONObject data;
    private JSONObject result;
    private String interfaceName;

    public LogTask(JSONObject data, JSONObject result, String interfaceName) {
        this.data = data;
        this.result = result;
        this.interfaceName = interfaceName;
    }

    @Override
    public void run() {
        if (data != null && data.containsKey(configBean.getParsed_data())) {
            LogBean logBean = new LogBean();
            JSONObject d_data = null;
            try {
                d_data = data.getJSONObject(configBean.getParsed_data());
                logBean.setInputparams(d_data.toJSONString());
            } catch (Exception e) {
                d_data = new JSONObject();
                logBean.setInputparams(data.getString(configBean.getParsed_data()));
            }
            UserBean userBean = (UserBean) data.get(configBean.getParsed_user());
            logBean.setOrgname(userBean.getOrganizationName());
            logBean.setSingleorgid(userBean.getOrganizationID());
            logBean.setUsername(userBean.getAccount());
            logBean.setOutputparams(result.toJSONString());
            logBean.setStatus(ResultStatusEnum.SUCCUSS.getCode().equals(result.getString("code")) ? "01" : "00");
            String accessType = d_data.getString("opetype");
            logBean.setAccesstype("");
            if (accessType != null && !StringUtils.isBlank(accessType)) {
                if (OperatorEnum.ADDORUPDATE.getVal().equals(accessType) || OperatorEnum.DELETE.getVal().equals(accessType)) {
                    logBean.setAccesstype(accessType);
                }
            }
            logBean.setInterfacename(interfaceName);
            logBean.setId(IdGenerateUtil.getKey());
            logMapper.addLog(logBean);
        }
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}
