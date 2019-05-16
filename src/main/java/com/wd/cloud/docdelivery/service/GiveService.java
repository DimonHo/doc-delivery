package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;

import java.util.Optional;

/**
 * @Author: He Zhigang
 * @Date: 2019/4/18 14:46
 * @Description:
 */
public interface GiveService {

    /**
     * 获取指定状态的应助记录，自动修正数据库中多于的重复数据
     * @param helpRecordId
     * @param giveStatusEnum
     * @return
     */
    Optional<GiveRecord> getGiveRecord(Long helpRecordId, GiveStatusEnum giveStatusEnum);
}
