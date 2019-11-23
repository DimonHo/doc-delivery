package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.SdolServerApi;
import com.wd.cloud.docdelivery.pojo.entity.*;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.AsyncService;
import com.wd.cloud.docdelivery.service.LiteraturePlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @Author: He Zhigang
 * @Date: 2019/5/24 15:22
 * @Description:
 */
@Slf4j
@Service("asyncService")
public class AsyncServiceImpl implements AsyncService {

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    LiteraturePlanRepository literaturePlanRepository;

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    DocFileRepository docFileRepository;

    @Autowired
    SdolServerApi sdolServerApi;

    @Autowired
    LiteraturePlanService literaturePlanService;

    /**
     * 指定渠道状态直接为成功
     */
    private static final List<Long> CHANNEL_SEND = CollectionUtil.newArrayList(1L,7L);

    /**
     * 执行自动应助
     */
    @Async
    @Override
    public void autoGive(Long helpRecordId) {

        helpRecordRepository.findById(helpRecordId).ifPresent(helpRecord -> {
            DocFile reusingDocFile = docFileRepository.findByLiteratureIdAndReusingIsTrue(helpRecord.getLiteratureId());
            boolean flag;
            if (null != reusingDocFile) {
                flag = reusingGive(reusingDocFile, helpRecord);
            } else {
                flag = bigDbGive(helpRecord);
            }
            //如果求助不成功,则对求助请求进行排班记录分配
            if (!flag) {
                LiteraturePlan nowWatch = literaturePlanService.nowWatch();
                if (nowWatch != null) {
                    helpRecord.setWatchName(nowWatch.getUsername());
                    helpRecordRepository.save(helpRecord);
                }
            }
        });
    }

    /**
     * 数据平台应助
     *
     * @param helpRecord
     */
    public boolean bigDbGive(HelpRecord helpRecord) {
        boolean flag = false;
        try {
            Optional<Literature> literatureOptional = literatureRepository.findById(helpRecord.getLiteratureId());
            if (literatureOptional.isPresent()) {
                Literature literature = literatureOptional.get();
                ResponseModel<String> pdfResponse = sdolServerApi.search(literature);
                if (!pdfResponse.isError()) {
                    String fileId = pdfResponse.getBody();

                    DocFile docFile = docFileRepository.findByFileIdAndLiteratureId(fileId, literature.getId()).orElse(new DocFile());
                    docFile.setFileId(fileId).setLiteratureId(literature.getId()).setBigDb(true);
                    docFileRepository.save(docFile);

                    GiveRecord giveRecord = new GiveRecord();
                    giveRecord.setFileId(fileId)
                            .setType(GiveTypeEnum.BIG_DB.value())
                            .setGiverName(GiveTypeEnum.BIG_DB.name())
                            .setStatus(GiveStatusEnum.SUCCESS.value());
                    giveRecord.setHelpRecordId(helpRecord.getId());
                    giveRecordRepository.save(giveRecord);

                    helpRecord.setStatus(CHANNEL_SEND.contains(helpRecord.getHelpChannel())?
                            HelpStatusEnum.HELP_SUCCESSED.value():HelpStatusEnum.HELP_SUCCESSING.value())
                            .setFileId(fileId)
                            .setGiveType(GiveTypeEnum.BIG_DB.value())
                            .setGiverName(GiveTypeEnum.BIG_DB.name())
                            .setHandlerName(GiveTypeEnum.BIG_DB.name());
                    helpRecordRepository.save(helpRecord);
                    flag = true;
                }else{
                    log.error("全文服务返回错误消息:{}",pdfResponse.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("全文服务应助失败",e);
        }
        return flag;
    }

    /**
     * 自动应助
     *
     * @param helpRecord
     */
    public boolean reusingGive(DocFile reusingDocFile, HelpRecord helpRecord) {
        GiveRecord giveRecord = new GiveRecord();
        giveRecord.setFileId(reusingDocFile.getFileId())
                .setType(GiveTypeEnum.AUTO.value())
                .setGiverName(GiveTypeEnum.AUTO.name())
                .setStatus(GiveStatusEnum.SUCCESS.value())
                .setHelpRecordId(helpRecord.getId());
        giveRecordRepository.save(giveRecord);
        helpRecord.setStatus(CHANNEL_SEND.contains(helpRecord.getHelpChannel())?
                HelpStatusEnum.HELP_SUCCESSED.value():HelpStatusEnum.HELP_SUCCESSING.value())
                .setFileId(reusingDocFile.getFileId())
                .setGiveType(GiveTypeEnum.AUTO.value())
                .setGiverName(GiveTypeEnum.AUTO.name())
                .setHandlerName(GiveTypeEnum.AUTO.name());
        helpRecordRepository.save(helpRecord);
        return true;
    }
}
