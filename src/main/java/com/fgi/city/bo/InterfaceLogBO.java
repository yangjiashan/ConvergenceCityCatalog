package com.fgi.city.bo;

import java.util.List;
import java.util.Map;

/**
 * 接口调用日志信息类
 */
public class InterfaceLogBO {

    private String counts;
    private String page_count;
    private String page_now;
    private List<Map<String, String>> details;

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public String getPage_count() {
        return page_count;
    }

    public void setPage_count(String page_count) {
        this.page_count = page_count;
    }

    public String getPage_now() {
        return page_now;
    }

    public void setPage_now(String page_now) {
        this.page_now = page_now;
    }

    public List<Map<String, String>> getDetails() {
        return details;
    }

    public void setDetails(List<Map<String, String>> details) {
        this.details = details;
    }
}
