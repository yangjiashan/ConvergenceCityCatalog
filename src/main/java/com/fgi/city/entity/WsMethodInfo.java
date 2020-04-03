package com.fgi.city.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 * @author sirc_yjs
 * @Description wsdl描述信息
 * @date 2019年3月1日
 */
public class WsMethodInfo {
    private String methodName; // 方法名称
    private String methodDesc; // 方法描述
    private Map<String, Map<?, ?>> methodName2InputParam = new HashMap<String, Map<?, ?>>();
    private String targetNameSpace; // 命名空间
    private String methodSoapAction; // soapaction
    private String endPoint;
    private String targetXsd;
    private List<String> inputNames; // 输入参数
    private List<String> inputType; // 输入类型
    private List<String> inputDesc; // 输入描述
    private List<String> outputNames; // 输出参数
    private List<String> outputType; // 输出类型
    private List<String> paramters; // 存放具体参数值（与名称一一对应）
    private String predealparam; // 处理前请求参数数字符串格式
    private String sep = "#";
    private Map<String, String> inputmap;
    protected static final Logger LOGGER = LoggerFactory.getLogger(WsMethodInfo.class);
    private final static String testparam = "1";

    public Map<String, String> getInputmap() {
        return inputmap;
    }

    public void setInputmap(Map<String, String> inputmap) {
        if (inputmap != null) {
            this.inputmap = inputmap;
        } else {
            this.inputmap = new HashMap<String, String>();
        }
    }

    public String getSep() {
        return sep;
    }

    public void setSep(String sep) {
        this.sep = sep;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getPredealparam() {
        return predealparam;
    }

    public void setPredealparam(String predealparam) {
        this.predealparam = predealparam;
    }

    public List<String> getParamters() {
        return paramters;
    }

    public void setParamters(List<String> paramters) {
        this.paramters = paramters;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getInputNames() {
        return this.inputNames;
    }

    public void setInputNames(List<String> inputNames) {
        this.inputNames = inputNames;
    }

    public Map<String, Map<?, ?>> getMethodName2InputParam() {
        return this.methodName2InputParam;
    }

    public void setMethodName2InputParam(
            Map<String, Map<?, ?>> methodName2InputParam) {
        this.methodName2InputParam = methodName2InputParam;
    }

    public String getTargetNameSpace() {
        return this.targetNameSpace;
    }

    public void setTargetNameSpace(String targetNameSpace) {
        this.targetNameSpace = targetNameSpace;
    }

    public String getMethodSoapAction() {
        return this.methodSoapAction;
    }

    public void setMethodSoapAction(String methodSoapAction) {
        this.methodSoapAction = methodSoapAction;
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getTargetXsd() {
        return this.targetXsd;
    }

    public void setTargetXsd(String targetXsd) {
        this.targetXsd = targetXsd;
    }

    public List<String> getOutputNames() {
        return this.outputNames;
    }

    public void setOutputNames(List<String> outputNames) {
        this.outputNames = outputNames;
    }

    public List<String> getInputType() {
        return this.inputType;
    }

    public void setInputType(List<String> inputType) {
        this.inputType = inputType;
    }

    public List<String> getOutputType() {
        return this.outputType;
    }

    public void setOutputType(List<String> outputType) {
        this.outputType = outputType;
    }

    public List<String> getInputDesc() {
        return this.inputDesc;
    }

    public void setInputDesc(List<String> inputDesc) {
        this.inputDesc = inputDesc;
    }

    /**
     * 组建soapaction（包含请求参数 参数值为'?'待传入）
     */
    public void madeNewString() {
        StringBuffer su = new StringBuffer();
        su.append(this.methodSoapAction == null ? "" : this.methodSoapAction);
        predealparam = su.toString();
    }


    /**
     * 设置请求参数值
     *
     * @param apisequence
     * @return
     * @throws Exception
     */
    public String getAndSetRequestParams(ApiSequence apisequence) throws Exception {
        // 设置请求参数值key-value形式到inputmap
        setInputmap(apisequence.getParametersMap());
        String deals = predealparam;
        String soap11 = "http://schemas.xmlsoap.org/soap/envelope/";
        String soap12 = "http://www.w3.org/2003/05/soap-envelope";
        boolean flag = false;
        if (deals.contains(soap12)) {
            deals = deals.replace(soap12, soap11);
            flag = true;
        }
        SOAPMessage msg = formatSoapString(deals);
        SOAPBody body = msg.getSOAPBody();
        try {
            // 键值对的方式，从map里面获取参数 key-value
            for (int i = 0; i < this.getInputNames().size(); i++) {
                String str = this.getInputNames().get(i);
                String v = "";
                // 如果str='tem:username' 则要截取username
                if (str != null && str.contains(":")) {
                    v = str.split(":")[1];
                } else {
                    v = str;
                }
                String val = this.inputmap.get(v);
                val = StringUtils.isBlank(val) ? testparam : val;
                NodeList list = body.getElementsByTagName(str);
                Node node = (Node) list.item(0);
                node.setTextContent(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.saveChanges();
        deals = soapMessageToString(msg);
        if (flag) {
            deals = deals.replace(soap11, soap12);
        }
        return deals;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    /**
     * 将string转soap
     *
     * @param soapString
     * @return
     */
    public static SOAPMessage formatSoapString(String soapString) {
        MessageFactory msgFactory;
        try {
            msgFactory = MessageFactory.newInstance();
            SOAPMessage reqMsg = msgFactory.createMessage(new MimeHeaders(),
                    new ByteArrayInputStream(soapString.getBytes("UTF-8")));
            reqMsg.saveChanges();
            return reqMsg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * soap转string
     *
     * @param message
     * @return
     */
    private String soapMessageToString(SOAPMessage message) {
        String result = null;
        if (message != null) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                result = baos.toString("UTF-8");
            } catch (Exception e) {
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException ioe) {
                        LOGGER.error(ExceptionUtils.getMessage(ioe));
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        String temp = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:web=\"http://WebXml.com.cn/\">"
                + "<soap:Header/>"
                + "<soap:Body>"
                + "<web:getWeatherbyCityName>"
                + " <web:theCityName>福建省</web:theCityName>"
                + "</web:getWeatherbyCityName>"
                + "</soap:Body>"
                + "</soap:Envelope>";

        String soap11 = "http://schemas.xmlsoap.org/soap/envelope/";
        String soap12 = "http://www.w3.org/2003/05/soap-envelope";
        boolean flag = false;
        if (temp.contains(soap12)) {
            temp = temp.replace(soap12, soap11);
            flag = true;
        }
        SOAPMessage msg = formatSoapString(temp);
        SOAPBody body = msg.getSOAPBody();
        NodeList list = body.getElementsByTagName("web:theCityName");
        Node node = (Node) list.item(0);
        System.out.println(node.getTextContent());
        System.out.println(list.getLength());
        msg.saveChanges();
        temp = new WsMethodInfo().soapMessageToString(msg);
        if (flag) {
            temp = temp.replace(soap11, soap12);
        }
        System.out.println(temp);
    }
}