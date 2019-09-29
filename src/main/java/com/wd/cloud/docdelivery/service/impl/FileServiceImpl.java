package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.wd.cloud.commons.exception.ExpException;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.FsServerApi;
import com.wd.cloud.docdelivery.feign.SdolServerApi;
import com.wd.cloud.docdelivery.model.DownloadFileModel;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import com.wd.cloud.docdelivery.repository.DocFileRepository;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.service.FileService;
import com.wd.cloud.docdelivery.service.GiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2018/5/16
 * @Description:
 */
@Slf4j
@Service("fileService")
public class FileServiceImpl implements FileService {

    @Autowired
    Global global;

    @Autowired
    DocFileRepository docFileRepository;

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    FsServerApi fsServerApi;
    @Autowired
    GiveService giveService;

    @Autowired
    SdolServerApi sdolServerApi;

    @Override
    public DownloadFileModel getDownloadFile(Long helpRecordId) {
        DownloadFileModel downloadFileModel = null;
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findByIdAndStatus(helpRecordId, HelpStatusEnum.HELP_SUCCESSED.value());
        if (optionalHelpRecord.isPresent()) {
            HelpRecord helpRecord = optionalHelpRecord.get();
            if (checkTimeOut(helpRecord.getGmtModified())) {
                throw new ExpException("文件已过期");
            }
            downloadFileModel = buildDownloadModel(helpRecord);
        }
        return downloadFileModel;
    }

    @Override
    public DownloadFileModel getWaitAuditFile(Long helpRecordId) {
        Optional<HelpRecord> helpRecordRow = helpRecordRepository.findByIdAndStatus(helpRecordId, HelpStatusEnum.WAIT_AUDIT.value());
        return helpRecordRow.map(this::buildDownloadModel).orElse(null);
    }

    private DownloadFileModel buildDownloadModel(HelpRecord helpRecord) {
        String fileId = helpRecord.getFileId();
        Literature literature = literatureRepository.findById(helpRecord.getLiteratureId()).orElse(null);
        String docTitle = literature != null ? literature.getDocTitle() : null;
        //以文献标题作为文件名，标题中可能存在不符合系统文件命名规范，在这里规范一下。
        docTitle = FileUtil.cleanInvalid(docTitle);
        DownloadFileModel downloadFileModel = new DownloadFileModel();
        ResponseModel<byte[]> responseModel = helpRecord.getGiveType() == GiveTypeEnum.BIG_DB.value() ?
                sdolServerApi.getFileByte(fileId) : fsServerApi.getFileByte(fileId);
        if (responseModel.isError()) {
            log.error("文件服务调用失败：{}", responseModel.getMessage());
            return null;
        }
        downloadFileModel.setFileByte(responseModel.getBody());
        String fileType = FileUtil.extName(responseModel.getMessage());
        downloadFileModel.setDownloadFileName(docTitle + "." + fileType);
        return downloadFileModel;
    }


    private boolean checkTimeOut(Date startDate) {
        return 15 < DateUtil.betweenDay(startDate, new Date(), true);
    }

    @Override
    public String getDownloadUrl(Long helpRecordId) {
        return global.getCloudHost() + "/doc-delivery/file/download/" + helpRecordId;
    }

}
