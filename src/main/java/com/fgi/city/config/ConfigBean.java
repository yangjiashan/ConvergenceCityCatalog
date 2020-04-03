package com.fgi.city.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConfigBean {
    private Map<String, String> areaCodeMap = new HashMap<>();
//    @Value("${guidUrl}")
//    private String guidurl;
    @Value("${singonWSDL}")
    private String singonWSDL;
    @Value("${hjsm2prikey}")
    private String hjsm2prikey;
    @Value("${hjsm2pubkey}")
    private String hjsm2pubkey;
    @Value("${parsed_data}")
    private String parsed_data;
    @Value("${parsed_user}")
    private String parsed_user;
    @Value("${parsed_guid}")
    private String parsed_guid;
    @Value("${parsed_sgid}")
    private String parsed_sgid;
    @Value("${logKeepDay}")
    private String logKeepDay;
    @Value("${autoCleanLogCronExpression}")
    private String autoCleanLogCronExpression;
    @Value("${autoCleanlogEnable}")
    private String autoCleanlogEnable;
    @Value("${autoGenerateClassOutPath}")
    private String autoGenerateClassOutPath;
    @Value("${autoPackageName}")
    private String autoPackageName;
    @Value("${area_01}")
    private String area_01;
    @Value("${area_02}")
    private String area_02;
    @Value("${area_03}")
    private String area_03;
    @Value("${area_04}")
    private String area_04;
    @Value("${area_05}")
    private String area_05;
    @Value("${area_06}")
    private String area_06;
    @Value("${area_07}")
    private String area_07;
    @Value("${area_08}")
    private String area_08;
    @Value("${area_09}")
    private String area_09;
    @Value("${area_10}")
    private String area_10;
    @Value("${interfaceExecuteUrl}")
    private String interfaceExecuteUrl;

    @PostConstruct
    public void init() {
        areaCodeMap.put("01", area_01);
        areaCodeMap.put("02", area_02);
        areaCodeMap.put("03", area_03);
        areaCodeMap.put("04", area_04);
        areaCodeMap.put("05", area_05);
        areaCodeMap.put("06", area_06);
        areaCodeMap.put("07", area_07);
        areaCodeMap.put("08", area_08);
        areaCodeMap.put("09", area_09);
        areaCodeMap.put("10", area_10);
    }

    public String getInterfaceExecuteUrl() {
        return interfaceExecuteUrl;
    }

    public void setInterfaceExecuteUrl(String interfaceExecuteUrl) {
        this.interfaceExecuteUrl = interfaceExecuteUrl;
    }

    public Map<String, String> getAreaCodeMap() {
        return areaCodeMap;
    }

    public void setAreaCodeMap(Map<String, String> areaCodeMap) {
        this.areaCodeMap = areaCodeMap;
    }

    public String getArea_01() {
        return area_01;
    }

    public void setArea_01(String area_01) {
        this.area_01 = area_01;
    }

    public String getArea_02() {
        return area_02;
    }

    public void setArea_02(String area_02) {
        this.area_02 = area_02;
    }

    public String getArea_03() {
        return area_03;
    }

    public void setArea_03(String area_03) {
        this.area_03 = area_03;
    }

    public String getArea_04() {
        return area_04;
    }

    public void setArea_04(String area_04) {
        this.area_04 = area_04;
    }

    public String getArea_05() {
        return area_05;
    }

    public void setArea_05(String area_05) {
        this.area_05 = area_05;
    }

    public String getArea_06() {
        return area_06;
    }

    public void setArea_06(String area_06) {
        this.area_06 = area_06;
    }

    public String getArea_07() {
        return area_07;
    }

    public void setArea_07(String area_07) {
        this.area_07 = area_07;
    }

    public String getArea_08() {
        return area_08;
    }

    public void setArea_08(String area_08) {
        this.area_08 = area_08;
    }

    public String getArea_09() {
        return area_09;
    }

    public void setArea_09(String area_09) {
        this.area_09 = area_09;
    }

    public String getArea_10() {
        return area_10;
    }

    public void setArea_10(String area_10) {
        this.area_10 = area_10;
    }

    public String getAutoGenerateClassOutPath() {
        return autoGenerateClassOutPath;
    }

    public void setAutoGenerateClassOutPath(String autoGenerateClassOutPath) {
        this.autoGenerateClassOutPath = autoGenerateClassOutPath;
    }

    public String getAutoPackageName() {
        return autoPackageName;
    }

    public void setAutoPackageName(String autoPackageName) {
        this.autoPackageName = autoPackageName;
    }

    public String getHjsm2prikey() {
        return hjsm2prikey;
    }

    public void setHjsm2prikey(String hjsm2prikey) {
        this.hjsm2prikey = hjsm2prikey;
    }

    public String getHjsm2pubkey() {
        return hjsm2pubkey;
    }

    public void setHjsm2pubkey(String hjsm2pubkey) {
        this.hjsm2pubkey = hjsm2pubkey;
    }

    public String getSingonWSDL() {
        return singonWSDL;
    }

    public void setSingonWSDL(String singonWSDL) {
        this.singonWSDL = singonWSDL;
    }

//    public String getGuidurl() {
//        return guidurl;
//    }

    public String getAutoCleanlogEnable() {
        return autoCleanlogEnable;
    }

    public void setAutoCleanlogEnable(String autoCleanlogEnable) {
        this.autoCleanlogEnable = autoCleanlogEnable;
    }

    public String getLogKeepDay() {
        return logKeepDay;
    }

    public void setLogKeepDay(String logKeepDay) {
        this.logKeepDay = logKeepDay;
    }

    public String getAutoCleanLogCronExpression() {
        return autoCleanLogCronExpression;
    }

    public void setAutoCleanLogCronExpression(String autoCleanLogCronExpression) {
        this.autoCleanLogCronExpression = autoCleanLogCronExpression;
    }

//    public void setGuidurl(String guidurl) {
//        this.guidurl = guidurl;
//    }

    public String getParsed_data() {
        return parsed_data;
    }

    public void setParsed_data(String parsed_data) {
        this.parsed_data = parsed_data;
    }

    public String getParsed_user() {
        return parsed_user;
    }

    public void setParsed_user(String parsed_user) {
        this.parsed_user = parsed_user;
    }

    public String getParsed_guid() {
        return parsed_guid;
    }

    public void setParsed_guid(String parsed_guid) {
        this.parsed_guid = parsed_guid;
    }

    public String getParsed_sgid() {
        return parsed_sgid;
    }

    public void setParsed_sgid(String parsed_sgid) {
        this.parsed_sgid = parsed_sgid;
    }
}
