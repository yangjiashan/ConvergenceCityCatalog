package com.fgi.city.template;

import com.fgi.city.bo.InterfaceBO;
import com.fgi.city.bo.MethodFieldBO;
import com.fgi.city.bo.MethodInfoBO;
import com.fgi.city.compiler.JavaStringCompiler;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.service.impl.CityQueryServiceImpl;
import com.fgi.city.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.xml.ws.Endpoint;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Generate_Template_Soap {

    public static Map<String, Endpoint> endpointMap = new HashMap<>();

    private transient Logger logger = LoggerFactory.getLogger(CityQueryServiceImpl.class);

    private FileUtil fileUtil = FileUtil.getInstance();

    private String changeLineStr = "\r\n";

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    @Qualifier(Bus.DEFAULT_BUS_ID)
    private SpringBus bus;


    /**
     * 返回自动生成类内容
     *
     * @param className
     * @param interfaceId
     * @return
     */
    public String readTemplenate_Soap(String className, String interfaceId) {
        String methodFile = "";
        String classFile = "";
        try {
            // 读取模板文件流
            methodFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "Template_Soap_Method.txt").getPath();
            classFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "Template_Soap_Class.txt").getPath();
        } catch (FileNotFoundException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
        // 读取模板成字符串
        String content = fileUtil.ReadFile(methodFile, "utf-8");
        // 读取模板成字符串
        String classContent = fileUtil.ReadFile(classFile, "utf-8");
        StringBuilder stringBuilder = new StringBuilder();
        // 根据interfaceid获取输入参数
        InterfaceBO interfaceBO = interfaceMapper.queryInterface(interfaceId);
        if (interfaceBO != null) {
            List<MethodInfoBO> methodInfoBOList = interfaceBO.getMethodinfos();
            if (methodInfoBOList != null) {
                for (MethodInfoBO bo : methodInfoBOList) {
                    String methodId = bo.getId();
                    String methodName = bo.getMethod_ename();
                    // 查询对应输入参数(去除token、账号、密码类型) 以sortnum排序
                    List<MethodFieldBO> methodFieldBOS = interfaceMapper.queryOhterFieldByMethodId(methodId);
                    if (methodFieldBOS == null) {
                        methodFieldBOS = new ArrayList<>();
                    }
                    StringBuilder params_content = new StringBuilder();
                    params_content.append("@WebParam(name = \"guid\") String guid, ");
                    for (MethodFieldBO mf : methodFieldBOS) {
                        params_content.append("@WebParam(name = \"" + mf.getParamname() + "\") String " + mf.getParamname() + ", ");
                    }
                    // 去除最后一个逗号
                    if (methodFieldBOS.size() > 0) {
                        params_content = params_content.deleteCharAt(params_content.lastIndexOf(","));
                    }
                    String tempContent = content;
                    // 方法映射替换
                    tempContent = tempContent.replace("template_method_name", methodName);
                    // 方法参数替换
                    tempContent = tempContent.replace("template_params", params_content.toString());
                    // 方法接口ID参数值替换
                    tempContent = tempContent.replace("template_interface_id", interfaceId);
                    // 方法接口方法ID参数值替换
                    tempContent = tempContent.replace("template_method_id", methodId);
                    // 添加方法
                    stringBuilder.append(tempContent).append(changeLineStr);
                }
            }
            // 类名替换
            classContent = classContent.replace("Template_Soap_Class", className);
            // 方法替换
            classContent = classContent.replace("template_method_content", stringBuilder.toString());
        }
        if (stringBuilder.length() > 0) {
            return classContent;
        } else {
            return null;
        }
    }

    // 编译后使用自定义加载器加载进虚拟机
    public Boolean soapCreateGenerate(String interfaceId) {
        try {
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
            // 文件名称（类名称）使用 "$" + interfaceId
            String className = "$" + serviceUrl.split("/")[1];
            String content = readTemplenate_Soap(className, interfaceId);
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
            publish(clzMul, serviceUrl, 0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("动态创建接口错误ws", ExceptionUtils.getMessage(e));
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String temp = "/temps";
        System.out.println(temp.split("/")[1]);
    }

    //动态发布接口 operate: 0->发布接口， -1->取消发布接口
    public synchronized void publish(Class<?> clzMul, String serviceUrl, int operate) throws Exception {
        if (operate == 0) {
            // 发布
            Endpoint endpoint = null;
            if (endpointMap.containsKey(serviceUrl)) {
                endpoint = endpointMap.get(serviceUrl);
            } else {
                endpoint = new EndpointImpl(bus, clzMul.newInstance());
            }
            if (endpoint == null) {
                return;
            }
            if (endpoint.isPublished()) {
                endpoint.stop();
                endpoint = new EndpointImpl(bus, clzMul.newInstance());
            }
            endpoint.publish(serviceUrl);
            endpointMap.put(serviceUrl, endpoint);
        } else if (operate == -1) {
            // 取消发布
            if (endpointMap.containsKey(serviceUrl)) {
                Endpoint endpoint = endpointMap.get(serviceUrl);
                if (endpoint != null) {
                    endpoint.stop();
                }
                endpointMap.remove(serviceUrl);
            }
        }
    }
}
