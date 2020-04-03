//package com.fgi.city.filter;
//
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.apache.cxf.helpers.IOUtils;
//import org.apache.cxf.interceptor.Fault;
//import org.apache.cxf.message.Message;
//import org.apache.cxf.phase.AbstractPhaseInterceptor;
//import org.apache.cxf.phase.Phase;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.OutputStream;
//
//@Component
//public class SoapOutInterceptor extends AbstractPhaseInterceptor<Message> {
//
//    public SoapOutInterceptor() {
//        //这儿使用pre_stream，意思为在流关闭之前
////        super(Phase.PRE_STREAM);
//
//        super(Phase.USER_STREAM);
//    }
//
//    @Override
//    public void handleMessage(Message message) throws Fault {
//        try {
//            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
//            message.setContent(OutputStream.class, byteArrayOutput);
//            message.getInterceptorChain().doIntercept(message);
//            ByteArrayOutputStream newByteArrayOutput = (ByteArrayOutputStream) message.getContent(OutputStream.class);
//            String str = new String(newByteArrayOutput.toByteArray(), "UTF-8");
//            str = StringEscapeUtils.unescapeXml(str);
//            System.out.println(str);
//            ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(str.getBytes("UTF-8"));
//            OutputStream os = message.getContent(OutputStream.class);
//            IOUtils.copy(byteArrayInput, os);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
