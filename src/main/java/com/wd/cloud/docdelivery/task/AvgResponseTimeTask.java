package com.wd.cloud.docdelivery.task;

import cn.hutool.core.util.RandomUtil;
import com.wd.cloud.docdelivery.model.AvgResponseTimeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author He Zhigang
 * @date 2019/2/27
 * @Description: 每隔一小时换平均时长
 */
@Async
@Component
public class AvgResponseTimeTask {

    @Autowired
    AvgResponseTimeModel avgResponseTimeModel;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void random() {
        long min = Math.round(avgResponseTimeModel.getAvgResponseTime() * 0.9);
        long max = Math.round(avgResponseTimeModel.getAvgResponseTime() * 1.1);
        min = min < 50 ? 50 : min;
        max = max > 120 ? 120 : max;
        avgResponseTimeModel.setAvgResponseTime(RandomUtil.randomLong(min, max));
        long minSuccess = Math.round(avgResponseTimeModel.getAvgSuccessResponseTime() * 0.9);
        long maxSuccess = Math.round(avgResponseTimeModel.getAvgSuccessResponseTime() * 1.1);
        minSuccess = minSuccess < 145 ? 145 : minSuccess;
        maxSuccess = maxSuccess > 325 ? 325 : maxSuccess;
        avgResponseTimeModel.setAvgSuccessResponseTime(RandomUtil.randomLong(minSuccess, maxSuccess));
    }
}
