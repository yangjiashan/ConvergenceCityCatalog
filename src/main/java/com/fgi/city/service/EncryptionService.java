package com.fgi.city.service;

import com.alibaba.fastjson.JSONObject;

public interface EncryptionService {

    void getGUID(JSONObject jsonData, JSONObject result);

    void getSecretKey(JSONObject jsonData, JSONObject result);

}
