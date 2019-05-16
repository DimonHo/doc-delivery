package com.wd.cloud.docdelivery.model;

/**
 * @author He Zhigang
 * @date 2018/10/13
 * @Description:
 */
public class DefaultMailFailedModel {
    /**
     * 邮件标题
     */
    private String mailTitle = "[文献互助•失败]-%s";
    /**
     * 文献标题
     */
    private String docTitle;


    public DefaultMailFailedModel() {
    }

    public String getMailTitle() {
        return mailTitle;
    }

    public DefaultMailFailedModel setMailTitle(String mailTitle) {
        this.mailTitle = mailTitle;
        return this;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public DefaultMailFailedModel setDocTitle(String docTitle) {
        this.docTitle = docTitle;
        return this;
    }


}
