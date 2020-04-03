package com.fgi.city.Test;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.enums.ResultStatusEnum;
import com.fgi.city.utils.NETUserSignonUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@Component
@WebService(targetNamespace = "http://tempuri.org/")
public class TestSoap2 {
    @Autowired
    private NETUserSignonUtil netUserSignonUtil;

    @WebMethod
    public String testSoap2(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        Document responseDoc = DocumentHelper.createDocument();
        Element responseElement = responseDoc.addElement("Response");
        Element responseCode = responseElement.addElement("code");
        Element responseMessage = responseElement.addElement("message");
        Element contentMessage = responseElement.addElement("content");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",username);
        jsonObject.put("password",password);
        responseCode.setText(ResultStatusEnum.SUCCUSS.getCode());
        responseMessage.setText(ResultStatusEnum.SUCCUSS.getDesc());
        contentMessage.setText(jsonObject.toString());
        return responseDoc.asXML();
    }

    @WebMethod
    public String testSoapNeedGuid2(@WebParam(name = "guid") String guid, @WebParam(name = "content") String content) {
        Document responseDoc = DocumentHelper.createDocument();
        Element responseElement = responseDoc.addElement("Response");
        Element responseCode = responseElement.addElement("code");
        Element responseMessage = responseElement.addElement("message");
        Element contentMessage = responseElement.addElement("content");
        // 验证guid是否有效
        // 判断guid是否是有效的
        if (!netUserSignonUtil.IsValidguid(guid)) {
            // guid无效
            responseCode.setText(ResultStatusEnum.FAILURE.getCode());
            responseMessage.setText("guid无效");
            return responseDoc.asXML();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resultMsg", content);
        responseCode.setText(ResultStatusEnum.SUCCUSS.getCode());
        responseMessage.setText(ResultStatusEnum.SUCCUSS.getDesc());
        contentMessage.setText(jsonObject.toString());
        return responseDoc.asXML();
    }

}
