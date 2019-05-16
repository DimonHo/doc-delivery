package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.model.DownloadFileModel;

/**
 * @author He Zhigang
 * @date 2018/5/16
 * @Description:
 */
public interface FileService {

    DownloadFileModel getDownloadFile(Long helpRecordId);

    DownloadFileModel getWaitAuditFile(Long helpRecordId);

    String getDownloadUrl(Long helpRecordId);

}
