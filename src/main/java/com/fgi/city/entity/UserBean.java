package com.fgi.city.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户
 */
public class UserBean implements Serializable {

    private static final long serialVersionUID = -315029089636057818L;

    //办公电话
    private String OfficePhone;
    //用户ID
    private String ID;
    //用户帐号，也是唯一值
    private String Account;
    //用户姓名（一般是中文名），显示时使用
    private String DisplayName;
    //用户生日
    private String Birthday;
    //性别（男、女）
    private String Sex;
    //家庭电话
    private String Telephone;
    //移动电话
    private String Mobile;
    //用户邮箱
    private String Email;
    //家庭地址
    private String Address;
    //用户行政级别
    private String ULevel;
    //用户职务
    private String Title;
    //用户职称
    private String Position;
    //CA认证的标识
    private String CAID;
    //用户数字帐号，用于消息中心语音登录
    private String AccountID;
    //用户排序序号
    private String OrderID;
    //用户是否可用，默认为0（0表示可用，1表示不可用）
    private String IsEnable;
    //所属机构ID
    private String OrganizationID;
    //直属部门ID
    private String DepartmentID;
    //用户描述
    private String Remark;
    //密码
    private String PassWord;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 所属机构名称
    private String organizationName;
    // 直属部门名称
    private String departmentName;


    public String getPassWord() {
        return PassWord;
    }

    public void setPassWord(String passWord) {
        PassWord = passWord;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getOfficePhone() {
        return OfficePhone;
    }

    public void setOfficePhone(String officePhone) {
        OfficePhone = officePhone;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public String getAccount() {
        return Account;
    }

    public void setAccount(String account) {
        Account = account;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    public String getTelephone() {
        return Telephone;
    }

    public void setTelephone(String telephone) {
        Telephone = telephone;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getULevel() {
        return ULevel;
    }

    public void setULevel(String uLevel) {
        ULevel = uLevel;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPosition() {
        return Position;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public String getCAID() {
        return CAID;
    }

    public void setCAID(String cAID) {
        CAID = cAID;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public String getOrderID() {
        return OrderID;
    }

    public void setOrderID(String orderID) {
        OrderID = orderID;
    }

    public String getIsEnable() {
        return IsEnable;
    }

    public void setIsEnable(String isEnable) {
        IsEnable = isEnable;
    }

    public String getOrganizationID() {
        return OrganizationID;
    }

    public void setOrganizationID(String organizationID) {
        OrganizationID = organizationID;
    }

    public String getDepartmentID() {
        return DepartmentID;
    }

    public void setDepartmentID(String departmentID) {
        DepartmentID = departmentID;
    }

    /**
     * 获取所有字符串类型的字段
     *
     * @return List<Field>
     */
    public List<Field> getStringField() {
        List<Field> results = new ArrayList<Field>();
        Field[] fields = UserBean.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if ("String".equals(fields[i].getType().getSimpleName())) {
                results.add(fields[i]);
            }
        }
        return results;
    }

}
