package com.wd.cloud.docdelivery.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.VHelpRecordRepository;
import com.wd.cloud.docdelivery.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
        List<VHelpRecord> bySend = vHelpRecordRepository.findBySend(false);
        if (CollectionUtil.isNotEmpty(bySend)) {
            mailService.sendMail(RandomUtil.randomEle(bySend));
        }

    }
}
