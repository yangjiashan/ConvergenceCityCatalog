package com.fgi.city.utils;

import com.fgi.city.config.ConfigBean;
import com.fgi.city.entity.UserBean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 调用.NET单点系统UserSignon webservice工具类
 */
@Component
public class NETUserSignonUtil {
    @Autowired
    private ConfigBean configBean;
    private transient Logger log = LogManager.getLogger(NETUserSignonUtil.class);
    private JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
    private org.apache.cxf.endpoint.Client client = null;

    @PostConstruct
    public void init() {
        client = dcf.createClient(configBean.getSingonWSDL());
    }

    /**
     * 获取guid
     *
     * @param userName
     * @param passWord
     * @return
     */
    public String getGUid(String userName, String passWord) {
        String guid = "";
        try {
            Object[] guidObjects = client.invoke("LoginByAccount", userName, passWord);
            guid = guidObjects[0].toString();
        } catch (Exception e) {
            log.info(ExceptionUtils.getStackTrace(e));
        }
        return guid;
    }

    /**
     * 根据guid获取用户信息
     *
     * @param guid 验证码guid
     * @return
     */
    public UserBean getUserInfo(String guid) {
        UserBean users = new UserBean();
        try {
            Object[] guidObjects = client.invoke("GetUserInfo", guid);
            users = getUser(guidObjects[0].toString());
        } catch (Exception e) {
            log.info(ExceptionUtils.getStackTrace(e));
        }
        return users;
    }

    /**
     * 验证当前的验证码是否有效
     *
     * @param guid 验证码guid
     * @return 如果验证码guid有效，延长该guid的有效时间并返回true，否则返回false
     */
    public boolean IsValidguid(String guid) {
        boolean flag = false;
        try {
            Object[] guidObjects = client.invoke("IsValidguid", guid);
            flag = Boolean.valueOf(guidObjects[0].toString());
        } catch (Exception e) {
            log.info(ExceptionUtils.getStackTrace(e));
        }
        return flag;
    }

    /**
     * 根据XML解析出用户数据，异常返回null
     *
     * @param userInfoXml
     * @return UserBean
     */
    private static UserBean getUser(String userInfoXml) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(userInfoXml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        UserBean users = new UserBean();
        Node node = document.selectSingleNode("//Account");
        if (node != null) {
            users.setAccount(node.getText());
        }
        node = document.selectSingleNode("//ID");
        if (node != null) {
            users.setID(node.getText());
        }
        node = document.selectSingleNode("//AccountID");
        if (node != null) {
            users.setAccountID(node.getText());
        }
        node = document.selectSingleNode("//DisplayName");
        if (node != null) {
            users.setDisplayName(node.getText());
        }
        node = document.selectSingleNode("//Address/Street");
        if (node != null) {
            users.setAddress(node.getText());
        }
        node = document.selectSingleNode("//Birthday");
        if (node != null) {
            users.setBirthday(node.getText());
        }
        node = document.selectSingleNode("//Email");
        if (node != null) {
            users.setEmail(node.getText());
        }
        node = document.selectSingleNode("//Telephone");
        if (node != null) {
            users.setTelephone(node.getText());
        }
        node = document.selectSingleNode("//OfficePhone");
        if (node != null) {
            users.setOfficePhone(node.getText());
        }
        node = document.selectSingleNode("//ULevel");
        if (node != null) {
            users.setULevel(node.getText());
        }
        node = document.selectSingleNode("//Position");
        if (node != null) {
            users.setPosition(node.getText());
        }
        node = document.selectSingleNode("//OrderID");
        if (node != null) {
            users.setOrderID(node.getText());
        }
        node = document.selectSingleNode("//DepartmentID");
        if (node != null) {
            users.setDepartmentID(node.getText());
        }
        node = document.selectSingleNode("//InstitutionID");
        if (node != null) {
            users.setOrganizationID(node.getText());
        }
        node = document.selectSingleNode("//Remark");
        if (node != null) {
            users.setRemark(node.getText());
        }
        node = document.selectSingleNode("//Sex");
        if (node != null) {
            users.setSex(node.getText());
        }
        node = document.selectSingleNode("//Title");
        if (node != null) {
            users.setTitle(node.getText());
        }
        node = document.selectSingleNode("//OrganizationName");
        if (node != null) {
            users.setOrganizationName(node.getText());
        }
        node = document.selectSingleNode("//DepartmentName");
        if (node != null) {
            users.setDepartmentName(node.getText());
        }
        node = document.selectSingleNode("//Organization");
        if (node != null) {
            Node nodeName = node.selectSingleNode("//Name");
            if (nodeName != null) {
                users.setOrganizationName(nodeName.getText());
            }
            List<Node> nodeIDs = node.selectNodes("//ID");
            if (nodeIDs != null) {
                users.setOrganizationID(nodeIDs.get(1).getText());
            }
        }
        return users;
    }

    public static void main(String[] args) {
        try {
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            org.apache.cxf.endpoint.Client client = dcf
                    .createClient("http://193.100.100.18:807/newsignonwebservice/UserSignOn.asmx?wsdl");
//            Object[] guidObjects = client.invoke("GetUserInfo", "60c74d35-6921-4342-b7fa-4a07e4572165");
//            String retMsg = guidObjects[0].toString();
//            UserBean users = getUser(retMsg);
//            System.out.println(users);
//            System.out.println(users.getOrganizationID());
//            boolean flag = false;
//            Object[] guidObjects = client.invoke("IsValidguid", "2d986eb1-0fb2-47f6-999f-3dadd8ebed9f");
//            flag = Boolean.valueOf(guidObjects[0].toString());
//            System.out.println(flag);
//            xmtest/sirc@1234
            Object[] guidObjects = client.invoke("LoginByAccount", "sirc_yjs", "sirc@123");
            System.out.println(guidObjects[0].toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
