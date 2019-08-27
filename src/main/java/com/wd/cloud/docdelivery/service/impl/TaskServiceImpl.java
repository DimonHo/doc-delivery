package com.wd.cloud.docdelivery.service.impl;

import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Author: He Zhigang
 * @Date: 2019/8/27 16:45
 * @Description:
 */
@Slf4j
@Service("taskService")
@Transactional(rollbackFor = Exception.class)
public class TaskServiceImpl implements TaskService {

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Override
    public void giveTimeout() {
        List<GiveRecord> giveRecords = giveRecordRepository.findTimeOutRecord();
        giveRecords.forEach(this::updateHelpStatus);
    }

    private void updateHelpStatus(GiveRecord giveRecord) {
        // 修改giveRecode的状态为超时
        giveRecord.setStatus(GiveStatusEnum.TIME_OUT.value());
        log.info("已超时：{}的应助超过15分钟没有上传文件",giveRecord.getGiverName());
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(giveRecord.getHelpRecordId());
        // 重置helpRecode的状态为待应助，且清除giver字段信息
        optionalHelpRecord.ifPresent(helpRecord -> helpRecord.setStatus(0).setGiverName(null).setGiveType(null));
    }
}
