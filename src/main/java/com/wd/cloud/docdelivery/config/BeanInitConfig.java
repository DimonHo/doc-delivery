package com.wd.cloud.docdelivery.config;

import com.wd.cloud.docdelivery.model.AvgResponseTimeModel;
import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.repository.LiteraturePlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

/**
 * @author He Zhigang
 * @date 2019/2/27
 * @Description:
 */
@Configuration
public class BeanInitConfig {

    @Bean
    public AvgResponseTimeModel avgResponseTimeModel() {
        return new AvgResponseTimeModel(100, 217);
    }

}
