package com.fgi.city;

import com.fgi.city.dao.CityReportMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
public class CityApplicationTests {
    @Autowired
    private CityReportMapper cityReportMapper;

    @Test
    public void contextLoads() {
//        ApiSequence apiSequence = new ApiSequence();
//        apiSequence.setApimethod("WEBSERVICE");
//        apiSequence.setUrl("http://localhost:10003/ws/WsOkController?wsdl");
//        apiSequence.setMaxconnectionseconds("10");
//        apiSequence.setParamters("{\"params\":\"aa\"}");
//        apiSequence.setUrlmethod("helloGoods");
//        apiSequence.setMethodtype("0");
//        apiSequence.setCondition("testHttp");
//        WebserviceClientHandler webserviceClientHandler = new WebserviceClientHandler(apiSequence);
//        webserviceClientHandler.execute();
//        System.out.println(webserviceClientHandler.getOutput());
//        System.out.println(webserviceClientHandler.getStatuscode());
//        System.out.println(webserviceClientHandler.getBody());

//        apiSequence.setUrl("http://localhost:10003/testHttpPost");
//        apiSequence.setCondition("testHttp");
//        apiSequence.setParamstype("0"); // json格式
//        apiSequence.setApimethod(ApiMethodEnum.GET.toString());
//        apiSequence.setParamters("{\"params\":\"aa\"}");
//        HttpClientHandler ClientHandler = new HttpClientHandler(apiSequence);
//        System.out.println(ClientHandler.execute());
//        System.out.println(ClientHandler.getOutput());
//        System.out.println(ClientHandler.getStatusCode());

//        ClientHandler clientHandler = new ClientHandler("45665832");
//        System.out.println(clientHandler.execute());


        // 查询接口旧值
        Map<String, String> interfaceOldInfo = cityReportMapper.getInterfaceInfoById("1111");
        System.out.println(interfaceOldInfo);
    }

}
