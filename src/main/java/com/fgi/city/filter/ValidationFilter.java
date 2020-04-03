//package com.fgi.city.filter;
//
//import com.alibaba.fastjson.JSONException;
//import com.alibaba.fastjson.JSONObject;
//import com.fgi.city.context.BeanProvider;
//import com.fgi.city.dao.SecretKeyMapper;
//import com.fgi.city.entity.SecretKeyBean;
//import com.fgi.city.entity.UserBean;
//import com.fgi.city.enums.FailReasonEnum;
//import com.fgi.city.service.impl.HttpRequestService;
//import com.fgi.city.Utils.NETUserSignonUtil;
//import com.fgi.city.Utils.SM2Util;
//import com.fgi.city.Utils.Sm4Util;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.annotation.Order;
//import org.springframework.web.context.support.WebApplicationContextUtils;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//
//@Order(1)
//@WebFilter(filterName = "ValidationFilter", urlPatterns = "/*")
//public class ValidationFilter implements Filter {
//    private Logger logger = LoggerFactory.getLogger(ValidationFilter.class);
//    private NETUserSignonUtil netUserSignonUtil = BeanProvider.getBean(NETUserSignonUtil.class);
//    private HttpRequestService httpRequestService;
//    private SecretKeyMapper secretKeyMapper = BeanProvider.getBean(SecretKeyMapper.class);
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        System.out.println("进入过滤器。。。init。。。");
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        System.out.println("进入过滤器。。。doFilter。。。");
//        // 获取请求路径
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
////        ServletContext context = request.getServletContext();
////        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
////        httpRequestService = ctx.getBean(HttpRequestService.class);
//        String requestUrl = request.getRequestURI();
////        JSONObject result = new JSONObject();
////        // 解析附加在post请求body上的数据包
////        JSONObject packData = httpRequestService.getRequestJsonData(request, result);
////        if (packData == null)
////            return;
//        if (requestUrl.startsWith("/report/")) {
//            // 如果匹配到地市目录、接口上报接口URL
//
//        } else if (requestUrl.startsWith("/encrypt/")) {
//            // 如果匹配到获取凭证接口URL
//
//            if (requestUrl.startsWith("/encrypt/getsecretkey")) {
//                // 验证GUID合法性
//            }
//
//        } else if (requestUrl.startsWith("/query/")) {
//            // 如果匹配到获取省平台接口下发URL
//
//        }
//        filterChain.doFilter(servletRequest, servletResponse);
//    }
//
//    // 地市上报时候做一些参数的验证，GUID检查、验签、解密，成功返回结果，失败返回空
//    // 最好加个断路器，当单点服务挂掉或者响应时间太长时候并且调用者批量调用时候容易引起失败堆积
//    private JSONObject reportValidate(JSONObject packData, JSONObject result) {
//        String guid = httpRequestService.checkGuid(packData, result);
//        if (StringUtils.isBlank(guid))
//            return null;
//        // 根据guid获取用户信息
//        UserBean users = netUserSignonUtil.getUserInfo(guid);
//        // 验签
//        String sign = packData.getString("sign");
//        String data = packData.getString("data");
//        // 获取该单位的SM2公钥
//        SecretKeyBean SM2KeyBean = secretKeyMapper.querySM2KeyByOrgId(users.getOrganizationID());
//        if (SM2KeyBean == null || StringUtils.isBlank(SM2KeyBean.getSecretkey())) {
//            // 该用户所属的机构还未和省平台交换SM2公钥
//            result.put("message", FailReasonEnum.FAIL_28.getDesc());
//            return null;
//        }
//        boolean signResult = false;
//        try {
//            // 验签
//            signResult = SM2Util.getInstance().SM2Verify(data, SM2KeyBean.getSecretkey(), sign);
//        } catch (Exception e) {
//            logger.error(ExceptionUtils.getMessage(e));
//        }
//        if (!signResult) {
//            result.put("message", FailReasonEnum.FAIL_29.getDesc());
//            return null;
//        }
//        // 解密
//        // 获取该单位的SM4秘钥
//        SecretKeyBean secretKeyBean = secretKeyMapper.querySM4KeyByOrgId(users.getOrganizationID());
//        if (secretKeyBean == null || StringUtils.isBlank(secretKeyBean.getSecretkey())) {
//            // 该用户所属的机构还未配置SM4秘钥
//            result.put("message", FailReasonEnum.FAIL_15.getDesc());
//            return null;
//        }
//        String d_data = Sm4Util.getInstance().SM4Decrypt(data, "SM4/ECB/PKCS5Padding", secretKeyBean.getSecretkey(), null);
//        if (d_data == null) {
//            // 解密失败
//            result.put("message", FailReasonEnum.FAIL_19.getDesc());
//            return null;
//        }
//        //
//
//
//
//
//        return null;
//
//    }
//
//    @Override
//    public void destroy() {
//        System.out.println("进入过滤器。。。destroy。。。");
//    }
//
//
//}
