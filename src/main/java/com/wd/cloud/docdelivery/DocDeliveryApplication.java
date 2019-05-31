package com.wd.cloud.docdelivery;


import com.spring4all.swagger.EnableSwagger2Doc;
import com.wd.cloud.casspringbootstarter.EnableCasClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DocDeliveryApplication class
 *
 * @author hezhigang
 * @date 2018/04/08
 */
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableSwagger2Doc
@EnableFeignClients
@EnableCasClient
@SpringCloudApplication
public class DocDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocDeliveryApplication.class, args);
    }

}
