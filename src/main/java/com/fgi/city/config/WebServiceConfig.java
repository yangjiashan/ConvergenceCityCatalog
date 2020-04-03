package com.fgi.city.config;

//import com.fgi.city.template.TestApiService;
//import com.fgi.city.filter.SoapOutInterceptor;

import com.fgi.city.Test.TestSoap;
import com.fgi.city.Test.TestSoap2;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class WebServiceConfig {
    @Bean
    public ServletRegistrationBean wsServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/ws/*");
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public TestSoap testSoap1() {
        return new TestSoap();
    }

    @Autowired
    public TestSoap2 testSoap2;

//    @Autowired
//    private SoapOutInterceptor soapOutInterceptor;

    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), testSoap1());
        endpoint.publish("/testSoap123");
//        endpoint.getInInterceptors().add(soapOutInterceptor);// 添加消息格式修改拦截器
        return endpoint;
    }

    @Bean
    public Endpoint endpoint2() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), testSoap2);
        endpoint.publish("/testSoap234");
        return endpoint;
    }

}