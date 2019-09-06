package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.SdolServerApi;
import com.wd.cloud.docdelivery.pojo.entity.*;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static int index = 0;

    @Autowired
    private List<LiteraturePlan> literaturePlans;

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
                Optional<LiteraturePlan> optionalLiteraturePlan = literaturePlans.stream()
                        .filter(literaturePlan -> DateUtil.isIn(new Date(),literaturePlan.getStartTime(),literaturePlan.getEndTime()))
                        .findAny();
                if (!optionalLiteraturePlan.isPresent()){
                    literaturePlans = literaturePlanRepository.findByNowWatch();
                    index = 0;
                }
                LiteraturePlan nowWatch;
                // 轮询排班
                if(CollectionUtil.isNotEmpty(literaturePlans)){
                    nowWatch = literaturePlans.get(index);
                    index = (index+1) % literaturePlans.size();
                }else{
                    nowWatch = literaturePlanRepository.findByNextWatch();
                }

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

                    helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSED.value())
                            .setFileId(fileId)
                            .setGiveType(GiveTypeEnum.BIG_DB.value())
                            .setGiverName(GiveTypeEnum.BIG_DB.name())
                            .setHandlerName(GiveTypeEnum.BIG_DB.name());
                    helpRecordRepository.save(helpRecord);
                    flag = true;
                }
            }
        } catch (Exception e) {
            log.error("数据库应助失败",e);
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
        helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSED.value())
                .setFileId(reusingDocFile.getFileId())
                .setGiveType(GiveTypeEnum.AUTO.value())
                .setGiverName(GiveTypeEnum.AUTO.name())
                .setHandlerName(GiveTypeEnum.AUTO.name());
        helpRecordRepository.save(helpRecord);
        return true;
    }
}
