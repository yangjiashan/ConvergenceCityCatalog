package com.fgi.city.utils;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import com.alibaba.fastjson.JSONObject;

public class GuidUtil {

    private volatile static GuidUtil guidUtil;

    private GuidUtil() {
    }

    public static GuidUtil getInstance() {
        if (guidUtil == null) {
            synchronized (GuidUtil.class) {
                if (guidUtil == null) {
                    guidUtil = new GuidUtil();
                }
            }
        }
        return guidUtil;
    }

    /**
     * 获取guid
     *
     * @param userid
     * @param password
     * @param guidurl
     * @return
     * @throws Exception
     */
    public String getGuid(String userid, String password, String guidurl) throws Exception {
        String guid = null;
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(guidurl);
        QName qName = new QName("http://tempuri.org/", "loginByAccount");
        call.setOperationName(qName);
        call.setUseSOAPAction(true);
        call.addParameter("userid", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("password", XMLType.XSD_STRING, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_STRING);
        String rest = (String) call.invoke(new Object[]{userid, password});
        JSONObject json = JSONObject.parseObject(rest);
        // 判断接口是否调用成功
        if ("01".equals(json.getString("code"))) {
            // 获取guid
            guid = json.getString("data");
        }
        return guid;
    }


    public static void main(String[] args) {
        try {
            String str = GuidUtil.getInstance().getGuid("sirc_yjs", "sirc@123", "http://193.100.100.225:8080/ConvergenceServiceBoot/webservice/Authentication");
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
