package com.wd.cloud.docdelivery.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author He Zhigang
 * @date 2018/12/25
 * @Description:
 */
@Accessors(chain = true)
@Data
@ApiModel(value = "个人统计数据")
public class MyTjDTO {

    /**
     * 可求助总数量
     */
    @ApiModelProperty(value = "总求助上限", example = "100")
    private Long total;

    /**
     * 每日可求助总数量
     */
    @ApiModelProperty(value = "今日求助上限", example = "5")
    private Long todayTotal;

    /**
     * 总剩余量
     */
    @ApiModelProperty(value = "总剩余求助次数", example = "95")
    private Long restTotal;

    /**
     * 今日剩余求助数量
     */
    @ApiModelProperty(value = "今日剩余求助次数", example = "3")
    private Long todayRestTotal;

    /**
     * 我的总应助次数
     */
    @ApiModelProperty(value = "我的总应助次数", example = "23")
    private Long giveCount;

    /**
     * 我的已求助总数量
     */
    @ApiModelProperty(value = "我的总求助次数", example = "5")
    private Long helpCount;

    /**
     * 我的今日已求助总数量
     */
    @ApiModelProperty(value = "我今日的求助次数", example = "2")
    private Long todayHelpCount;
    /**
     * 我求助成功的数量
     */
    @ApiModelProperty(value = "我的求助成功总次数", example = "16")
    private Long successHelpCount;

    /**
     * 我应助成功的数量
     */
    @ApiModelProperty(value = "我的应助成功总数", example = "13")
    private Long successGiveCount;


}
