package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;

/**
 * @Author: He Zhigang
 * @Date: 2019/5/24 15:22
 * @Description: 异步service类
 */
public interface AsyncService {

    void autoGive(Long helpRecordId);
}
