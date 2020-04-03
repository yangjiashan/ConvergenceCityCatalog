package com.fgi.city.entity;

/**
 * 秘钥实体类
 */
public class SecretKeyBean {

    private String id;
    private String orgid; // 机构ID
    private String secretkey; // SM4秘钥
    private String ivkey;
    private String createtime; // 创建时间
    private String updatetime; // 更新时间
    private String ktype; // 秘钥类型（1 - > SM4秘钥，2 - > SM2公钥）

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public String getKtype() {
        return ktype;
    }

    public void setKtype(String ktype) {
        this.ktype = ktype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgid() {
        return orgid;
    }

    public void setOrgid(String orgid) {
        this.orgid = orgid;
    }

    public String getIvkey() {
        return ivkey;
    }

    public void setIvkey(String ivkey) {
        this.ivkey = ivkey;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }
}
