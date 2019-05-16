package com.wd.cloud.docdelivery.model;

import lombok.Data;

/**
 * @author He Zhigang
 * @date 2018/10/19
 * @Description:
 */
@Data
public class DefaultMailNotifyModel {
    /**
     * 邮件标题
     */
    private String mailTitle = "用户文献互助";
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 求助用户
     */
    private String helperName;

}
