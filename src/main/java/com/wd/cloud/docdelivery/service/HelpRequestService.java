package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.Literature;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/28 14:39
 * @Description:
 */
public interface HelpRequestService {

    /**
     * 文献互助请求
     *
     * @param literatureVo
     * @param helpRecord
     * @return
     */
    void helpRequest(Literature literatureVo, HelpRecord helpRecord);

}
