package com.fgi.city.compiler;

import com.fgi.city.context.BeanProvider;
import com.fgi.city.template.Generate_Template_Soap;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.jws.WebService;
import java.lang.reflect.Method;

public class MappingRegulator {

    private volatile static MappingRegulator mappingRegulator;

    private MappingRegulator() {
    }

    public static MappingRegulator getInstance() {
        if (mappingRegulator == null) {
            synchronized (MappingRegulator.class) {
                if (mappingRegulator == null) {
                    mappingRegulator = new MappingRegulator();
                }
            }
        }
        return mappingRegulator;
    }


    private Generate_Template_Soap createSoap = BeanProvider.getBean(Generate_Template_Soap.class);

    /**
     * controlCenter(运行时RequestMappingHandlerMapping中添加、删除、修改Mapping接口)
     *
     * @param controllerClass 希望加载的类Class
     * @param Context         spring上下文
     * @param type            1新增 2修改 3删除
     * @param serviceUrl
     * @throws IllegalAccessException
     * @throws Exception
     */
    public void controlCenter(Class<?> controllerClass, ApplicationContext Context, Integer type, String serviceUrl) throws IllegalAccessException, Exception {
        //获取RequestMappingHandlerMapping
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) Context.getBean("requestMappingHandlerMapping");
        Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
        //设置私有属性为可见
        getMappingForMethod.setAccessible(true);
        if (controllerClass.getAnnotation(WebService.class) != null) {
            createSoap.publish(controllerClass, serviceUrl, 0);
        } else {
            //获取类中的方法
            Method[] method_arr = controllerClass.getMethods();
            for (Method method : method_arr) {
                //判断方法上是否有注解RequestMapping
                if (method.getAnnotation(RequestMapping.class) != null) {
                    //获取到类的RequestMappingInfo
                    RequestMappingInfo mappingInfo = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, method, controllerClass);
                    if (type == 1) {
                        //注册
                        registerMapping(requestMappingHandlerMapping, mappingInfo, controllerClass, method);
                    } else if (type == 2) {
                        //取消注册
                        unRegisterMapping(requestMappingHandlerMapping, mappingInfo);
                        registerMapping(requestMappingHandlerMapping, mappingInfo, controllerClass, method);
                    } else if (type == 3) {
                        unRegisterMapping(requestMappingHandlerMapping, mappingInfo);
                    }
                }
            }
        }
    }

    /**
     * registerMapping(注册mapping到spring容器中)
     *
     * @param requestMappingHandlerMapping
     * @Exception 异常对象
     */
    public static void registerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping, RequestMappingInfo mappingInfo, Class<?> controllerClass, Method method) throws Exception, IllegalAccessException {
        requestMappingHandlerMapping.registerMapping(mappingInfo, controllerClass.newInstance(), method);
    }

    /**
     * unRegisterMapping(spring容器中删除mapping)
     *
     * @param requestMappingHandlerMapping
     * @Exception 异常对象
     */
    public static void unRegisterMapping(RequestMappingHandlerMapping requestMappingHandlerMapping, RequestMappingInfo mappingInfo) throws Exception, IllegalAccessException {
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);
    }
}
