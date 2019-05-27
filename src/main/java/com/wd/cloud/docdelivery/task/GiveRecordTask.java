package com.wd.cloud.docdelivery.task;

import com.wd.cloud.commons.util.DateUtil;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author He Zhigang
 * @date 2018/7/17
 * @Description: 定时删除用户过期的应助记录
 */
@Slf4j
@Async
@Component
public class GiveRecordTask {
    @Autowired
    ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Scheduled(fixedRate = 1000 * 60 * 15)
    public void deleteGiveRecord() {
        List<GiveRecord> giveRecords = giveRecordRepository.findByTypeAndStatus(GiveTypeEnum.USER.value(), GiveStatusEnum.WAIT_UPLOAD.value());
        giveRecords.forEach(this::updateHelpStatus);
    }

    private void updateHelpStatus(GiveRecord giveRecord) {
        Date startTime = DateUtil.offsetMinute(giveRecord.getGmtCreate(), 15);
        log.info("执行时间:{}", startTime);
        threadPoolTaskScheduler.schedule(new GiveTimeOutTask(), startTime);
    }
}
