package com.wd.cloud.docdelivery.pojo.dto;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author He Zhigang
 * @date 2018/12/25
 * @Description:
 */
@Data
@Accessors(chain = true)
public class TjDto {

    /**
     * 求助总量
     */
    private Long total;
    /**
     * 求助成功总量
     */
    private Long successTotal;
    /**
     * 求助总成功率
     */
    private String successRate;


    /**
     * 今日求助总量
     */
    private Long todayTotal;
    /**
     * 今日求助成功数量
     */
    private Long todaySuccessTotal;
    /**
     * 今日求助成功率
     */
    private String todaySuccessRate;


    public String getSuccessRate() {
        return this.total != null && this.total > 0 ? NumberUtil.formatPercent((double) this.successTotal / (double) this.total, 2) : "100%";
    }

    public String getTodaySuccessRate() {
        return this.todayTotal != null && this.todayTotal > 0 ? NumberUtil.formatPercent((double) this.todaySuccessTotal / (double) this.todayTotal, 2) : "100%";
    }
}
