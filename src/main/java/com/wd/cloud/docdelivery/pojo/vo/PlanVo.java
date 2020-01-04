package com.wd.cloud.docdelivery.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: He Zhigang
 * @Date: 2019/7/1 11:18
 * @Description:
 */
@Data
@ApiModel(value = "排班计划")
public class PlanVo {

    /**
     * 被排班人
     */
    @ApiModelProperty(value = "被排班人", example = "xiaoyuan")
    private String username;

    /**
     * 安排人
     */
    @ApiModelProperty(value = "排班人", example = "liujing")
    private String arranger;

    @ApiModelProperty(value = "计划开始时间", example = "2019-06-30 00:00:00")
    private Date startTime;

    @ApiModelProperty(value = "计划开始时间", example = "2019-06-30 08:00:00")
    private Date endTime;

    @ApiModelProperty(value = "顺序", example = "1")
    private Integer orderList;
}
