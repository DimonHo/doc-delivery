package com.wd.cloud.docdelivery.model;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author He Zhigang
 * @date 2019/1/7
 * @Description:
 */
@Data
@Accessors(chain = true)
public class MailTemplateModel {

    private String template;
    /**
     * 邮件标题
     */
    private String mailTitle;

    private String docTitle;

    /**
     * 下载链接
     */
    private String downloadUrl;
    /**
     * 有效期毫秒数
     */
    private int exp = 1000 * 60 * 60 * 24 * 15;

    /**
     * 有效期
     */
    private String expStr = DateUtil.offsetMillisecond(DateTime.now(), exp).toString("yyyy-MM-dd HH:mm:ss");

    /**
     * 渠道名称
     */
    private String channelName;
    /**
     * 渠道官网
     */
    private String channelUrl;

    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 求助邮箱
     */
    private String helperEmail;

}
