package com.wd.cloud.docdelivery;


import com.wd.starter.casspringbootstarter.EnableCasClient;
import com.wd.starter.swaggerspringbootstarter.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * DocDeliveryApplication class
 *
 * @author hezhigang
 * @date 2018/04/08
 */
@EnableAsync
@EnableJpaAuditing
@EnableSwagger2Doc
@EnableFeignClients
@EnableCasClient
@SpringBootApplication
public class DocDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocDeliveryApplication.class, args);
    }

}
