package com.wd.cloud.docdelivery.service.impl;

import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.service.GiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @Author: He Zhigang
 * @Date: 2019/4/18 14:47
 * @Description:
 */
@Service("giveService")
public class GiveServiceImpl implements GiveService {

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Override
    public Optional<GiveRecord> getGiveRecord(Long helpRecordId, GiveStatusEnum giveStatusEnum) {
        Optional<List<GiveRecord>> giveRecordOptional = giveRecordRepository.findByHelpRecordIdAndStatus(helpRecordId, giveStatusEnum.value());
        if (giveRecordOptional.isPresent()) {
            List<GiveRecord> giveRecords = giveRecordOptional.get();
            // 如果有多条第三方应助，删除多余的脏数据
            while (giveRecords.size() > 1) {
                giveRecordRepository.delete(giveRecords.stream().findFirst().get());
                giveRecords.remove(0);
            }
            return Optional.of(giveRecords.get(0));
        }
        return Optional.empty();
    }
}
