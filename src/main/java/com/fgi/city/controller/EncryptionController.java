package com.fgi.city.controller;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.service.EncryptionService;
import com.fgi.city.service.impl.HttpRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/encrypt")
public class EncryptionController {

    @Autowired
    private HttpRequestService httpRequestService;

    @Autowired
    private EncryptionService encryptionService;

    /**
     * 获取凭证接口（密文返回）
     *
     * @param request
     * @return
     */
    @PostMapping("/getGUID")
    public String getGUID(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        JSONObject jsonData = httpRequestService.getRequestJsonData(request, result);
        if (jsonData == null)
            return result.toJSONString();
        // 获取guid
        encryptionService.getGUID(jsonData, result);
        return result.toJSONString();
    }

    /**
     * 获取SM4秘钥（密文返回）
     *
     * @param request
     * @return
     */
    @PostMapping("/getsecretkey")
    public String getSecretKey(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        JSONObject jsonData = httpRequestService.getRequestJsonData(request, result);
        if (jsonData == null)
            return result.toJSONString();
        // 获取sm4
        encryptionService.getSecretKey(jsonData, result);
        return result.toJSONString();
    }
}
