package com.wd.cloud.docdelivery.model;

/**
 * @author He Zhigang
 * @date 2018/10/13
 * @Description:
 */
public class DefaultMailSuccessModel {
    /**
     * 邮件标题
     */
    private String mailTitle = "[文献互助•成功]-%s";
    /**
     * 文献标题
     */
    private String docTitle;
    /**
     * 下载链接
     */
    private String downloadUrl;


    public DefaultMailSuccessModel() {
    }

    public String getMailTitle() {
        return mailTitle;
    }

    public DefaultMailSuccessModel setMailTitle(String mailTitle) {
        this.mailTitle = mailTitle;
        return this;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public DefaultMailSuccessModel setDocTitle(String docTitle) {
        this.docTitle = docTitle;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public DefaultMailSuccessModel setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }


}
