package com.wd.cloud.docdelivery.feign;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.pojo.dto.MailMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: He Zhigang
 * @Date: 2019/5/14 15:25
 * @Description:
 */
@FeignClient(value = "mail-server", url = "${feign.url.mail-server}")
public interface MailServerApi {

    /**
     * 发送邮件
     *
     * @param business
     * @param mailMessage
     * @return
     */
    @PostMapping("/send/{business}/{businessId}")
    ResponseModel send(@PathVariable String business, @PathVariable String businessId, @RequestBody MailMessage mailMessage);

}
