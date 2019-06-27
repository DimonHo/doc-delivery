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
@FeignClient(value = "mail-server", url= "http://cloud.hnlat.com/mail-server", fallback = MailServerApi.Fallback.class)
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

    @Component("mailServerApi")
    class Fallback implements MailServerApi {

        @Override
        public ResponseModel send(String business, String businessId, MailMessage mailMessage) {
            return ResponseModel.fail().setMessage("邮件服务异常");
        }
    }
}
