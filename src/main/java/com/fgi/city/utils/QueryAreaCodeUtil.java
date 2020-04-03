package com.fgi.city.utils;

import com.fgi.city.config.ConfigBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueryAreaCodeUtil {

    @Autowired
    private ConfigBean configBean;

    public String getAreaCodeID(String areacode) {
        Map<String, String> areaCodeMap = configBean.getAreaCodeMap();
        if (areaCodeMap.containsKey(areacode)) {
            return areaCodeMap.get(areacode);
        }
        return "";
    }

}
