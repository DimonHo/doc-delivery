package com.wd.cloud.docdelivery.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Hu Langshi
 * @date 2019/10/15
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "文献求助原始数据post对象")
public class HelpRawModel {

    /**
     * 是否匿名
     */
    @ApiModelProperty(value = "是否匿名,默认0", example = "0")
    private Boolean anonymous;

    @ApiModelProperty(value = "求助渠道,默认0", example = "1")
    @NotNull
    private Long helpChannel;

    @ApiModelProperty(value = "求助的邮箱", example = "hulangshi@qq.com")
    @NotBlank
    private String helperEmail;

    @ApiModelProperty(value = "求助者IP")
    @NotNull
    private String helperIp;

    @ApiModelProperty(value = "求助者用户名")
    private String helperName;

    @ApiModelProperty(value = "求助的学校ID")
    private String orgFlag;

    @ApiModelProperty(value = "求助的学校名称")
    private String orgName;

    @ApiModelProperty(value = "求助的信息", example = "doi:01923959101,xxx:102030104")
    @NotBlank
    private String info;

    @ApiModelProperty(value = "求助记录ID,默认0")
    private Long helpRecordId;

    @ApiModelProperty(value = "是否有效：0:待处理,1:无效,2:有效，默认0")
    private Long invalid;
}
