package com.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @ClassName EurekaServerSart3
 * @Description: TODO
 * @Author 小松
 * @Date 2019/7/8
 **/
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerSart3 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerSart3.class,args);
    }
}
