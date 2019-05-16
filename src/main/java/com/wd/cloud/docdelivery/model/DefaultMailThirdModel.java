package com.wd.cloud.docdelivery.model;

/**
 * @author He Zhigang
 * @date 2018/10/13
 * @Description:
 */
public class DefaultMailThirdModel {
    /**
     * 邮件标题
     */
    private String mailTitle = "[文献互助•疑难文献]-%s";
    /**
     * 文献标题
     */
    private String docTitle;


    public DefaultMailThirdModel() {
    }

    public String getMailTitle() {
        return mailTitle;
    }

    public DefaultMailThirdModel setMailTitle(String mailTitle) {
        this.mailTitle = mailTitle;
        return this;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public DefaultMailThirdModel setDocTitle(String docTitle) {
        this.docTitle = docTitle;
        return this;
    }

}
