package com.fgi.city;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableHystrix
@ServletComponentScan
@EnableScheduling
public class ConvergenceCityCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvergenceCityCatalogApplication.class, args);
    }


}
