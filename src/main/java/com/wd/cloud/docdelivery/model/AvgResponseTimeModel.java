package com.wd.cloud.docdelivery.model;

import lombok.Data;

/**
 * @author He Zhigang
 * @date 2019/2/27
 * @Description: 平均响应时长每小时随机一个数字
 */

@Data
public class AvgResponseTimeModel {

    /**
     * 平均响应时长 单位S
     */
    private long avgResponseTime;
    /**
     * 求助成功平均时长  单位S
     */
    private long avgSuccessResponseTime;

    public AvgResponseTimeModel(long avgResponseTime, long avgSuccessResponseTime) {
        this.avgResponseTime = avgResponseTime;
        this.avgSuccessResponseTime = avgSuccessResponseTime;
    }
}
