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

    @NotNull
    @ApiModelProperty(value = "求助渠道,默认0", example = "0")
    private Long helpChannel;

    @NotNull
    @ApiModelProperty(value = "求助的邮箱", example = "hulangshi@qq.com")
    private String helperEmail;

    @NotNull
    @ApiModelProperty(value = "求助者用户名")
    private String helperName;

    @ApiModelProperty(value = "求助的学校ID")
    private String orgFlag;

    @ApiModelProperty(value = "求助的学校名称")
    private String orgName;

    @NotBlank
    @ApiModelProperty(value = "求助的信息", example = "doi:01923959101,xxx:102030104")
    private String info;

}
