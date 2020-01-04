package com.wd.cloud.docdelivery.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author: He Zhigang
 * @Date: 2020/1/3 16:42
 * @Description: 导出求助记录
 */
@Data
public class ExcelRowDto {
    private Date gmtCreate;
    private String orgName;
    private String helperEmail;
    private String helperName;
    private String docTitle;
    private String docHref;
    private String watchName;
    private String handlerName;
    private String status;
}
