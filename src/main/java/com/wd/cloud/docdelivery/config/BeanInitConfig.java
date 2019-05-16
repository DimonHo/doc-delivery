package com.wd.cloud.docdelivery.config;

import com.wd.cloud.docdelivery.model.AvgResponseTimeModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author He Zhigang
 * @date 2019/2/27
 * @Description:
 */
@Configuration
public class BeanInitConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public AvgResponseTimeModel avgResponseTimeModel() {
        return new AvgResponseTimeModel(100, 217);
    }
}
