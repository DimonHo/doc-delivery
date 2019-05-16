package com.wd.cloud.docdelivery.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/28 14:07
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "文献求助post对象")
public class HelpRequestVO {

    @Valid
    HelperVO helper;

    @Valid
    LiteratureVO literature;
}
