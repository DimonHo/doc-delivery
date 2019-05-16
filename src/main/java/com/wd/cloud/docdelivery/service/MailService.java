package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;

/**
 * @author He Zhigang
 * @date 2018/5/17
 * @Description:
 */
public interface MailService {

    void sendMail(VHelpRecord vHelpRecord);
}
