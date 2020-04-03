package com.fgi.city.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fgi.city.entity.ApiSequence;
import com.fgi.city.entity.WsMethodInfo;
import com.fgi.city.utils.ImportWsdl;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.xml.XmlEscapers;

public class WebserviceClientHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(WebserviceClientHandler.class);
    protected static final int MS_TO_S_UNIT = 1000; // 秒到毫秒转换
    private ApiSequence apisequence;
    private String statuscode = "404";
    protected String output;
    private WsMethodInfo wsinfo = null;
    private String body = null;

    public WebserviceClientHandler(ApiSequence apisequence) {
        this.apisequence = apisequence;
    }

    /**
     * 执行接口探测
     *
     * @return
     */
    public void execute() {
        try {
            apisequence.setParamsToMap(); // 设置请求参数和请求头
            List<WsMethodInfo> list = null;
            synchronized (WebserviceClientHandler.class) {
                ImportWsdl ws = new ImportWsdl();
                list = ws.getProBySoap(apisequence.getUrl());
            }
            // 发送请求获得响应
            body = selectMethodAndRequest(list);
            if (body == null) {
                // wsdl文件解析错误，结束执行（方法执行要么抛异常，要么肯定会返回xml格式，所以当body为null时肯定是wsdl文件解析异常）
                LOGGER.error("wsdl文件解析错误，结束执行");
            }
            statuscode = "200";
            if ("-1111".equals(body)) {
                // 返回多个标签不知道怎么解析
                statuscode = "404";
                appendMessage("返回多个标签不知道怎么解析");
                return;
            }
            validResponse(body, statuscode);
        } catch (FileNotFoundException e1) {
            statuscode = "404";
            appendMessage(e1.toString());
            LOGGER.error("Send request to url[" + apisequence.getUrl() + "] failed", e1);
        } catch (ConnectException e2) {
            statuscode = "404";
            appendMessage(e2.toString());
            LOGGER.error("Send request to url[" + apisequence.getUrl() + "] failed", e2);
        } catch (Exception e3) {
            appendMessage(e3.toString());
            statuscode = "500";
            LOGGER.error("Send request to url[" + apisequence.getUrl() + "] failed", e3);
        }
    }

    /**
     * 选择方法并且请求 返回请求结果
     *
     * @param list
     * @return
     * @throws Exception
     */
    private String selectMethodAndRequest(List<WsMethodInfo> list) throws Exception {
        String result = "";
        if (list == null) {
            LOGGER.error("Send request to url[" + apisequence.getUrl() + "] failed wdsl文件解析错误");
            return null;
        }
        boolean flag = false;
        for (WsMethodInfo wi : list) {
            if (apisequence.getUrlmethod().equals(wi.getMethodName())) {
                wsinfo = wi;
                if (wsinfo.getOutputNames().size() <= 1) {
                    result = sendRequestBySoap(wi.getAndSetRequestParams(apisequence), apisequence.getUrl());
                    flag = true;
                } else {
                    return "-1111";
                }

            }
        }
        if (!flag) {
            // 无此方法
            LOGGER.error("Send request to url[" + apisequence.getUrl() + "] failed wdsl无此方法请检查方法名称或者参数-->方法：" + apisequence.getUrlmethod());
            return null;
        }
        return result;
    }


    /**
     * 发送webservice请求
     *
     * @param sendmsg
     * @param requrl
     * @return
     * @throws Exception
     */
    protected String sendRequestBySoap(String sendmsg, String requrl) throws Exception {
//        LOGGER.info("请求地址：" + apisequence.getUrl() + ";method->" + apisequence.getUrlmethod() + "; **->请求消息头：" + sendmsg);
        // 开启HTTP连接
        URL url = new URL(requrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        // 设置HTTP请求相关信息
        httpConn.setRequestProperty("Content-Length", String.valueOf(sendmsg.getBytes("utf-8").length));
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setRequestMethod("POST");
//		httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
//        httpConn.setConnectTimeout(Integer.parseInt(apisequence.getMaxconnectionseconds()) * MS_TO_S_UNIT);
//        httpConn.setReadTimeout(Integer.parseInt(apisequence.getMaxconnectionseconds()) * MS_TO_S_UNIT);
        // 进行HTTP请求
        OutputStream outObject = httpConn.getOutputStream();
        // statuscode = String.valueOf(httpConn.getResponseCode());
        outObject.write(sendmsg.getBytes("utf-8"));
        // 关闭输出流
        outObject.flush();
        outObject.close();
        // 获取HTTP响应数据
        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream(), "utf-8");
        BufferedReader inReader = new BufferedReader(isr);
        StringBuffer result = new StringBuffer();
        String inputLine;
        while ((inputLine = inReader.readLine()) != null) {
            result.append(inputLine);
        }
        // 打印HTTP响应数据
//        LOGGER.info("请求地址：" + apisequence.getUrl() + ";method->" + apisequence.getUrlmethod() + "; **->请求结果：" + result);
        // 关闭输入流
        inReader.close();
        isr.close();
        httpConn.disconnect();
        return result.toString();
    }

    public static void main(String[] args) {
        String sendmsg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <tem:insertPersonInfo>\n" +
                "         <!--Optional:-->\n" +
                "         \n" +
                "         <!--Optional:-->\n" +
                "         <password>1</password><username>1</username>\n" +
                "      </tem:insertPersonInfo>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String requesturl = "http://localhost:10003/ws/WsOkController?wsdl";
        try {
            System.out.println(new WebserviceClientHandler(null).sendRequestBySoap(sendmsg, requesturl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析body和状态码
     *
     * @param body
     * @param statusCode
     * @throws Exception
     */
    protected void validResponse(String body, String statusCode) throws Exception {
        if (!"200".equals(statusCode)) {
            appendMessage("Invalid status: " + apisequence.getUrl()
                    + " required: " + 200 + ", received: " + statusCode);
        } else {
            switch (apisequence.getConditiontype()) {
                case "CONTAINS":
                    if ("1".equals(apisequence.getMethodtype())) {
                        // 验证类特殊处理
                        if (StringUtils.isEmpty(body)
                                || !(body.contains("true") || body.contains("false")
                                || body.contains("是") || body.contains("否"))) {
                            appendMessage(apisequence.getUrl()
                                    + " doesn't contain "
                                    + XmlEscapers.xmlContentEscaper().escape(
                                    "true or false "));
                        }
                    } else {
                        if (StringUtils.isEmpty(body) || !body.contains(apisequence.getCondition())) {
                            appendMessage(apisequence.getUrl()
                                    + " doesn't contain "
                                    + XmlEscapers.xmlContentEscaper().escape(
                                    apisequence.getCondition()));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 提取结果，异常则返回null
     * 模板里有引用到不可删除
     *
     * @param result
     * @return
     */
    public Element getResultStr(String result, Element responseElement) {
        try {
            if (this.wsinfo != null) {
                Document document = DocumentHelper.parseText(result);
                List<String> params = this.wsinfo.getOutputNames();
                // 这边params目前只会有一个(对应soap返回的...response节点)
                for (String string : params) {
                    // response
                    Element element = document.getRootElement();
                    Element element_body = element.element("Body");
                    Element element_string = element_body.element(string);
                    List<Element> list = element_string.elements();
                    for (Element e : list) {
                        Element contentElement = responseElement.addElement("content");
                        if (e.isTextOnly()) {
//                            contentElement.setText(e.getText());
                            contentElement.addCDATA(e.getText());
                        } else {
                            List<Element> listInfo = e.elements();
                            for (Element info : listInfo) {
                                contentElement.add(info.createCopy());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getMessage(e));
        }
        return responseElement;
    }


//    /**
//     * 提取结果，异常则返回null
//     * 模板里有引用到不可删除
//     *
//     * @param result
//     * @return
//     */
//    public List<String> getResultStr(String result) {
//        List<String> resultList = null;
//        try {
//            if (this.wsinfo != null) {
//                Document document = DocumentHelper.parseText(result);
//                List<String> params = this.wsinfo.getOutputNames();
//                resultList = new ArrayList<>();
//                // 这边params目前只会有一个 ，多个不知道怎么解析
//                for (String string : params) {
//                    // response
//                    Element element = document.getRootElement();
//                    Element element_body = element.element("Body");
//                    Element element_string = element_body.element(string);
//                    List<Element> list = element_string.elements();
//                    for (Element e : list) {
//                        // result
//                        StringBuilder stringBuilder = new StringBuilder();
//                        if (e.isTextOnly()) {
//                            stringBuilder.append(e.getText());
//                        } else {
//                            List<Element> listInfo = e.elements();
//                            for (Element info : listInfo) {
//                                stringBuilder.append(info.asXML());
//                            }
//                        }
//                        resultList.add(stringBuilder.toString());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            LOGGER.error(ExceptionUtils.getMessage(e));
//        }
//        return resultList;
//    }

    // 拼接错误信息
    protected void appendMessage(String message) {
        if (output == null) {
            output = "";
        }
        if (message != null && !message.trim().isEmpty()) {
            output += message;
        }
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(String statuscode) {
        this.statuscode = statuscode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}