package com.fgi.city.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.entity.ApiSequence;
import com.fgi.city.enums.ApiMethodEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.xml.XmlEscapers;

public class HttpClientHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(HttpClientHandler.class);
    // Convert mill seconds to second unit
    protected static final int MS_TO_S_UNIT = 1000;
    // Normal http response code
    protected static final String NORMAL_RESPONSE_CODE = "200";
    // https prefix
    protected static final String HTTPS = "https";

    protected static HttpsTrustManager httpsTrustManager = new HttpsTrustManager();

    private ApiSequence apisequence;

    protected String output;

    private String statusCode = null;

    private String body = null;

    public HttpClientHandler(ApiSequence apisequence) {
        this.apisequence = apisequence;
    }

    /**
     * 执行接口探测
     *
     * @return
     */
    public String execute() {
        try {
            apisequence.setParamsToMap(); // 设置请求参数和请求头
            // 发送请求获得响应
            CloseableHttpResponse response = sendRequest();
            // 获得状态码
            statusCode = String.valueOf(getStatusCode(response));
            // 获取请求结果
            body = getResponseBody(response);
            response.close();
            // 将结果与用户设置的接口参数比较，是否返回正常
            validResponse(body, statusCode);
        } catch (Exception e) {
            appendMessage(e.toString());
            LOGGER.error("Send request to url[" + apisequence.getUrl() + "] failed", e);
        }
        return body;
    }

    /**
     * 发送http请求
     *
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected CloseableHttpResponse sendRequest() throws ClientProtocolException, IOException {
        // 构建请求
        RequestBuilder builder = createRequestBuilder();
        ApiMethodEnum paramthod = ApiMethodEnum.valueOf(apisequence.getApimethod());
        if (paramthod.equals(ApiMethodEnum.GET)) {
            // 如果是get请求
            addRequestParams(builder);
        } else if (paramthod.equals(ApiMethodEnum.POST)) {
            // 如果是post请求
            setHttpEntity(builder);
        }
        // 指定请求url
        HttpUriRequest request = builder.setUri(apisequence.getUrl()).build();
        // 设置请求头
        setHeaders(request);
        // 设置一些配置
        CloseableHttpClient client = createHttpClient();
        return client.execute(request);
    }

    /**
     * 获取请求结果
     *
     * @param httpResponse
     * @return
     * @throws ParseException
     * @throws IOException
     */
    protected String getResponseBody(CloseableHttpResponse httpResponse) throws ParseException, IOException {
        if (httpResponse == null) return null;
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) return null;
        String webPage = EntityUtils.toString(entity, "UTF-8");
        return webPage;
    }

    /**
     * 获得请求接口状态码
     *
     * @param httpResponse
     * @return
     */
    protected int getStatusCode(CloseableHttpResponse httpResponse) {
        int status = httpResponse.getStatusLine().getStatusCode();
        return status;
    }

    /**
     * 解析body和状态码
     *
     * @param body
     * @param statusCode
     * @throws Exception
     */
    protected void validResponse(String body, String statusCode) throws Exception {
        if (!NORMAL_RESPONSE_CODE.equals(statusCode)) {
            appendMessage("Invalid status: " + apisequence.getUrl() + " required: " + 200 + ", received: " + statusCode);
        } else {
            switch (apisequence.getConditiontype()) {
                case "CONTAINS":
                    if (StringUtils.isEmpty(body) || !body.contains(apisequence.getCondition())) {
                        appendMessage(apisequence.getUrl() + " doesn't contain "
                                + XmlEscapers.xmlContentEscaper().escape(apisequence.getCondition()));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public String getOutput() {
        return output;
    }

    protected void appendMessage(String message) {
        if (output == null) {
            output = "";
        }
        if (message != null && !message.trim().isEmpty()) {
            output += message;
        }
    }

    /**
     * 添加请求参数（get）
     *
     * @param builder
     */
    protected void addRequestParams(RequestBuilder builder) {
        HashMap<String, String> map = apisequence.getParametersMap();
        if (map == null || map.size() == 0) return;
        for (String key : map.keySet()) {
            String val = map.get(key);
            builder.addParameter(key, val);
        }
    }

    /**
     * 设置请求参数（post）
     *
     * @param builder
     */
    protected void setHttpEntity(RequestBuilder builder) {
        try {
            // 构建消息实体
            Map<String, String> map = apisequence.getParametersMap();
            List<NameValuePair> list = new ArrayList<>();
            HttpEntity httpEntity = null;
            if ("0".equals(apisequence.getParamstype())) {
                // 键值对
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpEntity = new UrlEncodedFormEntity(list, "UTF-8");
            } else if ("1".equals(apisequence.getParamstype())) {
                // json格式
                httpEntity = new StringEntity(JSONObject.toJSONString(map), "UTF-8");
            }
            if (httpEntity != null) {
                builder.setEntity(httpEntity);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 设置请求头
     *
     * @param request
     */
    protected void setHeaders(HttpUriRequest request) {
        HashMap<String, String> map = apisequence.getHeadersMap();
        if (map == null || map.size() == 0) return;
        for (String key : map.keySet()) {
            request.addHeader(key, map.get(key));
        }
    }

    /**
     * 生成https或者http请求端
     *
     * @return
     */
    protected CloseableHttpClient createHttpClient() {
        // 设置请求超时时间
        final RequestConfig requestConfig = requestConfig();
        HttpClientBuilder httpClientBuilder;
        if (isHttps()) {
            // 如果是http请求
            // Support SSL
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(createSSLContext());
            httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslConnectionSocketFactory);
        } else {
            httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
        }
        return httpClientBuilder.build();
    }

    /**
     * 设置请求超时时间
     *
     * @return
     */
    private RequestConfig requestConfig() {
        final int maxConnMillSeconds = Integer.valueOf(apisequence.getMaxconnectionseconds()) * MS_TO_S_UNIT;
        return RequestConfig.custom().setSocketTimeout(maxConnMillSeconds).setConnectTimeout(maxConnMillSeconds).build();
    }

    private SSLContext createSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new HttpsTrustManager[]{httpsTrustManager}, null);
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Create SSLContext error", e);
        }
    }

    /**
     * 判断是否是https请求
     *
     * @return
     */
    protected boolean isHttps() {
        return apisequence.getUrl().toLowerCase().startsWith(HTTPS);
    }

    /**
     * 根据用户所选请求方式构建请求
     *
     * @return
     */
    protected RequestBuilder createRequestBuilder() {
        ApiMethodEnum paramthod = ApiMethodEnum.valueOf(apisequence.getApimethod());
        if (paramthod.equals(ApiMethodEnum.GET)) {
            return RequestBuilder.get();
        } else if (paramthod.equals(ApiMethodEnum.POST)) {
            return RequestBuilder.post();
        } else if (paramthod.equals(ApiMethodEnum.HEAD)) {
            return RequestBuilder.head();
        } else if (paramthod.equals(ApiMethodEnum.PUT)) {
            return RequestBuilder.put();
        } else if (paramthod.equals(ApiMethodEnum.DELETE)) {
            return RequestBuilder.delete();
        } else {
            return null;
        }
    }

    /**
     * Default X509TrustManager implement
     */
    private static class HttpsTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // ignore
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // ignore
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


}