package com.wd.cloud.docdelivery.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/28 14:58
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "求助者")
public class HelperVo {

    @ApiModelProperty(value = "机构flag", example = "wdkj")
    @NotBlank
    private String orgFlag;

    @ApiModelProperty(value = "机构名称", example = "纬度科技")
    private String orgName;

    @ApiModelProperty(value = "求助渠道", example = "1：qq,2:spis,3:zhy,4:crs,5:paper")
    @NotNull
    private Long helpChannel;

    /**
     * 求助用户邮箱
     */
    @ApiModelProperty(value = "求助邮箱", example = "hezhigang@qq.com")
    @NotBlank
    @Email
    private String helperEmail;

    private String helperName;

    @ApiModelProperty(value = "求助更多", example = "doi:01923959101,xxx:102030104")
    private String remark;

    @ApiModelProperty(value = "是否匿名,默认false", example = "false")
    private Boolean anonymous;
}
