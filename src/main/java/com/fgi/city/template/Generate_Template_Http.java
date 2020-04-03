package com.fgi.city.template;

import com.fgi.city.compiler.JavaStringCompiler;
import com.fgi.city.compiler.MappingRegulator;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.context.BeanProvider;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.service.impl.CityQueryServiceImpl;
import com.fgi.city.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.util.Map;

@Component
public class Generate_Template_Http {

    private transient Logger logger = LoggerFactory.getLogger(CityQueryServiceImpl.class);
    private FileUtil fileUtil = FileUtil.getInstance();

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private InterfaceMapper interfaceMapper;

    /**
     * 返回自动生成类内容
     *
     * @param reqeustType
     * @param className
     * @param classMapper
     * @param methodMapper
     * @param methodName
     * @param interfaceId
     * @return
     */
    public String readTemplenate_get(String reqeustType, String className, String classMapper, String methodMapper, String methodName, String interfaceId) {
        // 读取模板文件流
        String javaFile = "";
        try {
            javaFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "Template_get.java").getPath();
        } catch (FileNotFoundException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
        // 读取模板成字符串
        String content = fileUtil.ReadFile(javaFile, "utf-8");
        // 替换文件内容
        // 类名映射替换
//        content = content.replace("template_class_mapper", classMapper);
        // 类名替换
        content = content.replace("Template_get", className);
        // 方法映射替换
        content = content.replace("template_method_mapper", methodMapper);
        // 方法名称替换
        content = content.replace("template_method_name", methodName);
        // 方法接口ID参数值替换
        content = content.replace("template_interface_id", interfaceId);
        // 方法接口请求类型替换
        String rqtype = setRequestType(reqeustType);
        content = content.replace("RequestMethod.POST", rqtype);
        return content;
    }

    // 获取请求方式
    private String setRequestType(String requestType) {
        String rqtype = "RequestMethod.POST";
        if ("POST".equals(requestType)) {
            rqtype = "RequestMethod.POST";
        } else if ("GET".equals(requestType)) {
            rqtype = "RequestMethod.GET";
        } else if ("HEAD".equals(requestType)) {
            rqtype = "RequestMethod.HEAD";
        } else if ("PUT".equals(requestType)) {
            rqtype = "RequestMethod.PUT";
        } else if ("DELETE".equals(requestType)) {
            rqtype = "RequestMethod.DELETE";
        } else if ("PATCH".equals(requestType)) {
            rqtype = "RequestMethod.PATCH";
        } else if ("OPTIONS".equals(requestType)) {
            rqtype = "RequestMethod.OPTIONS";
        } else if ("TRACE".equals(requestType)) {
            rqtype = "RequestMethod.TRACE";
        }
        return rqtype;
    }

    /**
     * http接口动态创建接口
     *
     * @param reqeustType
     * @param methodMapper
     * @param interfaceId
     * @param operate
     * @return
     */
    public Boolean httpCreateGenerate(String reqeustType, String methodMapper, String interfaceId, int operate) {
        try {
            // 文件名称（类名称）、方法名称 = “$” + 发布名称
            // 查询接口信息，查询不到返回false
            Map<String, String> interfaceInfo = interfaceMapper.queryInterfaceById(interfaceId);
            if (interfaceInfo == null) {
                logger.error("查询不到对应的接口信息！");
                return false;
            }
            // 查询该接口的发布地址
            String serviceUrl = interfaceInfo.get("PUBLISHURL");
            if (StringUtils.isBlank(serviceUrl) || !serviceUrl.startsWith("/")) {
                // 发布地址错误
                logger.error("发布地址不合法！");
                return false;
            }
            String className = "$" + serviceUrl.split("/")[1];
            String classMapper = "";
            String methodName = "$" + className;
            String content = readTemplenate_get(reqeustType, className, classMapper, methodMapper, methodName, interfaceId);
            if (content == null) {
                // 拼接class文件错误
                logger.error("拼接class文件错误");
                return false;
            }
            String javaNameStr = className + ".java";
            String classNameStr = className + ".class";
            String fullName = configBean.getAutoPackageName() + className;
            //动态编译class
            JavaStringCompiler compiler = new JavaStringCompiler();
            Map<String, byte[]> results = compiler.compile(javaNameStr, content);
            // 将class字节写入class文件，以便下次程序重启使用
            fileUtil.createFile(results.get(fullName), configBean.getAutoGenerateClassOutPath(), classNameStr);
            //加载class
            Class<?> clzMul = compiler.loadClass(fullName, results);
            //获取spring的applicationContext
            ApplicationContext applicationContext = BeanProvider.getApplicationContext();
            //注册接口到注册中心
            MappingRegulator.getInstance().controlCenter(clzMul, applicationContext, operate, "");
        } catch (Exception e) {
            logger.error("动态创建接口错误", e);
            return false;
        }
        return true;
    }


}
