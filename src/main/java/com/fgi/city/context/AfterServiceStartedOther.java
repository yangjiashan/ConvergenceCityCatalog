package com.fgi.city.context;

import com.fgi.city.compiler.JavaStringCompiler;
import com.fgi.city.compiler.MappingRegulator;
import com.fgi.city.config.ConfigBean;
import com.fgi.city.dao.InterfaceMapper;
import com.fgi.city.template.Generate_Template_Http;
import com.fgi.city.template.Generate_Template_Soap;
import com.fgi.city.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AfterServiceStartedOther implements CommandLineRunner {
    protected Logger logger = LoggerFactory.getLogger(AfterServiceStartedOther.class);
    @Autowired
    private ConfigBean configBean;
    @Autowired
    private InterfaceMapper interfaceMapper;
    @Autowired
    private Generate_Template_Http autoCreate;
    @Autowired
    private Generate_Template_Soap soapCreate;


    /**
     * 会在服务启动完成后立即执行
     */
    @Override
    public void run(String... args) throws Exception {
        // 使用自定义类加载器，加载自动封装的接口
        try {
//            getFiles(configBean.getAutoGenerateClassOutPath(), BeanProvider.getApplicationContext());
            // 以$+'接口ID'为文件名称，如果是本地文件夹内已经存在这个文件则直接加载这个文件，否则从接口表里面直接读取拼接编译载入
            // 每次启动都要从接口表中获取，如果接口状态有改变，这时候再去读取文件上的数据 就会有问题
            // 查询接口状态不是待发布的
            List<Map<String, String>> mapList = interfaceMapper.queryMethodListByState();
            int counts = 0, wcounts = 0, hcounts = 0, countsFail = 0, wcountsFail = 0, hcountsFail = 0;
            if (mapList != null) {
                for (Map<String, String> methodMap : mapList) {
                    // 判断是http还是webservice
                    String interfaceType = methodMap.get("INTERFACETYPE");
                    String publishurl = methodMap.get("PUBLISHURL");
                    String interfaceId = methodMap.get("INTERFACEID");
                    String methodId = methodMap.get("METHODID");
                    boolean presult = false;
                    if ("01".equals(interfaceType)) {
                        // webservice
                        presult = soapCreate.soapCreateGenerate(interfaceId);
                        if (presult) {
                            wcounts = wcounts + 1;
                        } else {
                            wcountsFail = wcountsFail + 1;
                        }
                    } else if ("02".equals(interfaceType)) {
                        // http
                        String requestType = methodMap.get("REQUESTTYPE");
                        presult = autoCreate.httpCreateGenerate(requestType, publishurl, interfaceId, 2);
                        if (presult) {
                            hcounts = hcounts + 1;
                        } else {
                            hcountsFail = hcountsFail + 1;
                        }
                    }
                    if (presult) {
                        counts = counts + 1;
                        logger.info("程序启动-发布接口，发布接口id:" + interfaceId + ",方法id:" + methodId);
                    } else {
                        countsFail = countsFail + 1;
                    }
                }
            }
            logger.info("程序启动完毕，接口查询数量：" + mapList.size() + "，成功启动接口：【" + counts + "】(Http:" + hcounts + "，WebService:" + wcounts + ")，启动失败：【" + countsFail + "】(http:" + hcountsFail + "，webservice:" + wcountsFail + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用自定义类加载器加载动态生成的class
     *
     * @param path
     * @param applicationContext
     * @throws Exception
     */
    public void getFiles(String path, ApplicationContext applicationContext) throws Exception {
        Map<String, byte[]> map = new HashMap<>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        JavaStringCompiler compiler = new JavaStringCompiler();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                File file_i = tempList[i];
                String fileName = file_i.getName();
                String[] fileNames = fileName.split(".class");
                //获取权限定名
                fileName = configBean.getAutoPackageName() + fileNames[0];
                //读取class到byte数组
                byte[] bytes = FileUtil.getInstance().FileToByte(tempList[i]);
                map.put(fileName, bytes);
                //加载class
                Class<?> clzMul = compiler.loadClass(fileName, map);
                //注册接口到注册中心
                MappingRegulator.getInstance().controlCenter(clzMul, applicationContext, 2, ("/" + fileNames[0]));
            }
        }
    }
}
