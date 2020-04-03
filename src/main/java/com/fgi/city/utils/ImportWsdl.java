package com.fgi.city.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.fgi.city.entity.WsMethodInfo;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIMultiThreadedHttpConnectionManager;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;

/**
 * @author sirc_yjs
 * @Description 解析wsdl
 * @date 2019年3月1日
 */
public class ImportWsdl {
    private Map<String, Object> paramterResult = new LinkedHashMap<String, Object>();
    private Map<String, Object> outPutResult = new LinkedHashMap<String, Object>();

    public Map<String, Object> getParamterResult() {
        return paramterResult;
    }

    public void setParamterResult(Map<String, Object> paramterResult) {
        this.paramterResult = paramterResult;
    }

    public Map<String, Object> getOutPutResult() {
        return outPutResult;
    }

    public void setOutPutResult(Map<String, Object> outPutResult) {
        this.outPutResult = outPutResult;
    }

    /**
     * 解析wsdl描述信息到WsMethodInfo方法信息中
     *
     * @param address
     * @return
     * @throws Exception
     */
    public List<WsMethodInfo> getProBySoap(String address) throws Exception {
        URL url = new URL(address);
        InputStream openStream = url.openStream();
        openStream.close();
        WsdlProject project = new WsdlProject();
        WsdlInterface[] wsdls = null;
        wsdls = WsdlImporter.importWsdl(project, address);
        List<Operation> operationList = wsdls[0].getOperationList();
        for (int i = 0; i < operationList.size(); i++) {
            Operation operation = operationList.get(i);
            WsdlOperation op = (WsdlOperation) operation;
            Map<String, Object> ptmp = new LinkedHashMap<String, Object>();
            Map<String, Object> otmp = new LinkedHashMap<String, Object>();
            ptmp.put(op.getName(), op.createRequest(true));
            paramterResult.put(i + "", ptmp);
            otmp.put(op.getName(), op.createResponse(true));
            outPutResult.put(i + "", otmp);
        }
        List<WsMethodInfo> list = null;
        if ((paramterResult != null) && (paramterResult.size() != 0)) {
            // 解析输入参数
            list = getParams(paramterResult);
            if (list != null && list.size() > 0) {
                // 解析输出参数
                list = getOutPuts(outPutResult, list);
            }
        }
        shutDownSoapUI();
        return list;
    }

    /**
     * 解析输入参数
     *
     * @param result
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<WsMethodInfo> getParams(Map<String, Object> result) throws Exception {
        List importWsdl = new ArrayList();
        Set keySet = result.keySet();
        Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            WsMethodInfo info = new WsMethodInfo();
            List<String> inputType = new ArrayList<String>();
            List<String> inputNames = new ArrayList<String>();
            List<String> isparent = new ArrayList<String>();
            String next = "" + iterator.next();
            HashMap hashMap = (HashMap) result.get(next);
            Set keySet2 = hashMap.keySet();
            Iterator iterator2 = keySet2.iterator();
            if (iterator2.hasNext()) {
                String next2 = (String) iterator2.next();
                String string = (String) hashMap.get(next2);
                //处理targetNameSpace
                String qname = string.substring(string.lastIndexOf("\"http://") + 1, string.lastIndexOf("\">"));
                info.setTargetNameSpace(qname);
                String soap11 = "http://schemas.xmlsoap.org/soap/envelope";
                String soap12 = "http://www.w3.org/2003/05/soap-envelope";
                InputStreamReader is = new InputStreamReader(new ByteArrayInputStream(string.getBytes("utf-8")));
                BufferedReader ibr = new BufferedReader(is);
                String readLine = ibr.readLine();
                if (readLine != null) {
                    if (readLine.indexOf(soap11) >= 0)
                        info.setTargetXsd("11");
                    else if (readLine.indexOf(soap12) >= 0) {
                        info.setTargetXsd("12");
                    }
                }
                ibr.close();
                is.close();
                info.setMethodSoapAction(string);
                Document read = DocumentHelper.parseText(string);
                Element rootElement = read.getRootElement();
                List<Element> elements = rootElement.elements();
                for (Element element : elements) {
                    if ("Body".equals(element.getName())) {
                        List<Element> elements2 = element.elements();
                        info.setMethodName(next2);
                        for (Element element2 : elements2) {
                            getParameter(element2, 1, 1, inputType, inputNames, isparent);
                            info.setInputNames(inputNames);
                            info.setInputType(inputType);
                            info.setOutputType(isparent);
                        }
                    }
                }
                info.madeNewString();
                importWsdl.add(info);
            }
        }
        return importWsdl;
    }

    /**
     * 解析输出参数
     *
     * @param result
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<WsMethodInfo> getOutPuts(Map<String, Object> result, List<WsMethodInfo> importWsdl) throws Exception {
        Set keySet = result.keySet();
        Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            List<String> outputType = new ArrayList<>();
            List<String> outputNames = new ArrayList<>();
            List<String> isparent = new ArrayList<>();
            String next = "" + iterator.next();
            WsMethodInfo info = importWsdl.get(Integer.parseInt(next));
            HashMap hashMap = (HashMap) result.get(next);
            Set keySet2 = hashMap.keySet();
            Iterator iterator2 = keySet2.iterator();
            if (iterator2.hasNext()) {
                String next2 = (String) iterator2.next();
                String string = (String) hashMap.get(next2);
                //处理targetNameSpace
                Document read = DocumentHelper.parseText(string);
                Element rootElement = read.getRootElement();
                List<Element> elements = rootElement.elements();
                for (Element element : elements) {
                    if ("Body".equals(element.getName())) {
                        List<Element> elements2 = element.elements();
                        // 默认只有一个返回 如“<s:element minOccurs="0" maxOccurs="1" name="loginByAccountResult" type="s:string"/>”
//                        for (Element element2 : elements2) {
//                            getOutParameter(element2, 1, 1, outputType, outputNames, isparent);
//                            info.setOutputNames(outputNames);
//                            info.setOutputType(isparent);
//                        }
                        // 修改为 获取返回值节点 例如：insertPersonInfoResponse
                        outputNames.add(elements2.get(0).getName());
                        info.setOutputNames(outputNames);
                    }
                }
            }
        }
        return importWsdl;
    }

    /**
     * 获取参数
     *
     * @param element2
     * @param gen
     * @param genParent
     * @param inputType
     * @param inputNames
     * @param isparent
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void getParameter(Element element2, int gen, int genParent, List<String> inputType, List<String> inputNames,
                                    List<String> isparent) {
        if (element2 != null) {
            List<Element> elements3 = element2.elements();
            if ((elements3 != null) && (elements3.size() != 0))
                for (Element element : elements3) {
                    inputType.add(gen + "," + genParent);
                    inputNames.add(element.getQualifiedName());
                    if (element != null) {
                        List e = element.elements();
                        if ((e != null) && (e.size() != 0)) {
                            isparent.add("1");
                            int gen1 = gen + gen;
                            getParameter(element, gen1, gen, inputType, inputNames, isparent);
                        } else {
                            isparent.add("0");
                        }
                    }
                }
        }
    }

    /**
     * 获取输出参数(只提取到方法名称下节点)
     *
     * @param element2
     * @param gen
     * @param genParent
     * @param inputType
     * @param inputNames
     * @param isparent
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void getOutParameter(Element element2, int gen, int genParent, List<String> inputType, List<String> inputNames,
                                       List<String> isparent) {
        if (element2 != null) {
            List<Element> elements3 = element2.elements();
            if ((elements3 != null) && (elements3.size() != 0))
                for (Element element : elements3) {
                inputType.add(gen + "," + genParent);
                inputNames.add(element.getName());
                if (element != null) {
                    List e = element.elements();
                    if ((e != null) && (e.size() != 0)) {
                        isparent.add("1");
                    } else {
                        isparent.add("0");
                    }
                }
            }
        }
    }

    /**
     * 关闭soapui其他无用线程
     */
    private static void shutDownSoapUI() {
        // 关闭soapui线程池
        SoapUI.getThreadPool().shutdown();
        try {
            SoapUI.getThreadPool().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 关闭soapui一些监听线程
        Thread[] tarray = new Thread[Thread.activeCount()];
        Thread.enumerate(tarray);
        for (Thread t : tarray) {
            if (t instanceof SoapUIMultiThreadedHttpConnectionManager.IdleConnectionMonitorThread) {
                ((SoapUIMultiThreadedHttpConnectionManager.IdleConnectionMonitorThread) t)
                        .shutdown();
            }
        }
        // 关闭soapui线程
        SoapUI.shutdown();
    }

    // 测试
    public static void main(String[] args) {
        try {
            ImportWsdl wl = new ImportWsdl();
            List<WsMethodInfo> result = wl.getProBySoap("http://localhost:10010/ConvergenceCityCatalog/ws/testSoap123?wsdl");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
