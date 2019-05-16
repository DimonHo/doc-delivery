package com.wd.cloud.docdelivery.task;

import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2019/1/4
 * @Description:
 */
public class GiveTimeOutTask implements Runnable {
    private GiveRecordRepository giveRecordRepository;
    private HelpRecordRepository helpRecordRepository;

    public GiveTimeOutTask(GiveRecordRepository giveRecordRepository, HelpRecordRepository helpRecordRepository) {
        this.giveRecordRepository = giveRecordRepository;
        this.helpRecordRepository = helpRecordRepository;
    }

    @Override
    public void run() {
        List<GiveRecord> giveRecords = giveRecordRepository.findTimeOutRecord();
        giveRecords.forEach(this::updateHelpStatus);
    }

    private void updateHelpStatus(GiveRecord giveRecord) {
        giveRecord.setStatus(GiveStatusEnum.TIME_OUT.value());
        giveRecordRepository.save(giveRecord);
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(giveRecord.getHelpRecordId());
        optionalHelpRecord.ifPresent(helpRecord -> {
            helpRecord.setStatus(0);
            helpRecordRepository.save(helpRecord);
        });
    }


}
