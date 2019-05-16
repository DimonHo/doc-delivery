package com.wd.cloud.docdelivery.model;

import cn.hutool.http.HtmlUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author He Zhigang
 * @date 2018/5/16
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "文献求助post对象")
public class HelpRequestModel {

    private String username;

    private String orgFlag;

    private String orgName;
    /**
     * 求助渠道
     */
    @ApiModelProperty(value = "求助渠道", example = "1")
    @NotNull
    private Long helpChannel;

    /**
     * 求助文件标题
     */
    @ApiModelProperty(value = "求助文献标题", example = "关于xxx可行性研究")
    @NotBlank
    private String docTitle;

    /**
     * 求助文献连接
     */
    @ApiModelProperty(value = "求助文献链接", example = "http://www.xxx.com")
    private String docHref;

    private String doi;

    private String issn;

    private String issue;

    private String volume;

    private String year;

    private String author;

    /**
     * 求助用户邮箱
     */
    @ApiModelProperty(value = "求助用户邮箱", example = "hezhigang@qq.com")
    @NotBlank
    @Email
    private String helperEmail;

    @ApiModelProperty(value = "补充信息", example = "doi:01923959101,xxx:102030104")
    private String remark;

    @ApiModelProperty(value = "是否匿名,默认false", example = "false")
    private Boolean anonymous;

    public String getDocTitle() {
        return HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(this.docTitle.trim()));
    }

    public String getDocHref() {
        return this.docHref != null ? this.docHref.trim() : null;
    }
}
