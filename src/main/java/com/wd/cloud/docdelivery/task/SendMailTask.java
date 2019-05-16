package com.wd.cloud.docdelivery.task;

import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.VHelpRecordRepository;
import com.wd.cloud.docdelivery.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Wu QiLong
 * @date 2018/12/17
 * @Description: 定时发送未发送的邮件
 */
@Async
@Component
public class SendMailTask {

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;

    @Autowired
    MailService mailService;

    @Scheduled(cron = "0/15 * * * * ?")
    public void updateGiveRecord() {
        //每隔15秒重发一封邮件，避免邮件发送频率过快
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.by("gmtCreate")));
        Page<VHelpRecord> bySend = vHelpRecordRepository.findBySend(false, pageable);
        bySend.forEach(vHelpRecord -> mailService.sendMail(vHelpRecord));
    }
}
