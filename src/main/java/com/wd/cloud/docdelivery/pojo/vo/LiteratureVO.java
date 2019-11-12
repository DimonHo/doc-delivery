package com.wd.cloud.docdelivery.pojo.vo;

import cn.hutool.http.HtmlUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/28 14:58
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "文献信息")
public class LiteratureVO {

    @ApiModelProperty(value = "求助文献标题", example = "关于xxx可行性研究")
    @NotBlank
    private String docTitle;

    @ApiModelProperty(value = "求助文献链接", example = "http://www.xxx.com")
    private String docHref;

    @ApiModelProperty(value = "求助文献doi", example = "xxxxxxx")
    private String doi;

    @ApiModelProperty(value = "期", example = "2")
    private String issue;

    @ApiModelProperty(value = "卷", example = "vol")
    private String volume;

    @ApiModelProperty(value = "年", example = "2018")
    private String year;

    @ApiModelProperty(value = "文献作者", example = "濯天临")
    private String author;

    @ApiModelProperty(value = "文献类型", example = "1：文献，2：图书，3：会议")
    private Integer type;

    /**
     * 防止调用者传过来的docTitle包含HTML标签，在这里将标签去掉
     */
    public String getDocTitle() {
        return this.docTitle != null ? HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(this.docTitle.trim())) : null;
    }

    public String getDocHref() {
        return this.docHref != null ? this.docHref.trim() : null;
    }
}
