package com.wd.cloud.docdelivery.service.impl;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.PdfSearchServerApi;
import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.repository.DocFileRepository;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.service.AsyncService;
import com.wd.cloud.docdelivery.service.HelpRequestService;
import com.wd.cloud.docdelivery.util.DocDeliveryArrangeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    GiveRecordRepository giveRecordRepository;

    @Autowired
    DocFileRepository docFileRepository;

    @Autowired
    PdfSearchServerApi pdfSearchServerApi;

    @Autowired
    HelpRequestService helpRequestService;

    /**
     * 执行自动应助
     */
    @Async
    @Override
    public void autoGive(HelpRecord helpRecord) {

        DocFile reusingDocFile = docFileRepository.findByLiteratureIdAndReusingIsTrue(helpRecord.getLiteratureId());
        boolean flag = false;
        if (null != reusingDocFile) {
            reusingGive(reusingDocFile, helpRecord);
            flag = true;
        } else {
            flag = bigDbGive(helpRecord);
        }
        //如果求助不成功,则对求助请求进行排班记录分配
        if (flag == false) {
            //查询排班人员
            LiteraturePlan literaturePlan = DocDeliveryArrangeUtils.getUserName();
            if (literaturePlan != null) {
                //helpRecord.setWatchName(literaturePlan.getUsername());
                helpRecordRepository.updateWatchName(helpRecord.getId(),literaturePlan.getUsername());
            }
        }

    }

    /**
     * 数据平台应助
     *
     * @param helpRecord
     */
    public boolean bigDbGive(HelpRecord helpRecord) {
        boolean[] flag = {true};
        try {
            literatureRepository.findById(helpRecord.getLiteratureId()).ifPresent(literature -> {
                ResponseModel<String> pdfResponse = pdfSearchServerApi.search(literature);
                if (!pdfResponse.isError()) {
                    String fileId = pdfResponse.getBody();

                    DocFile docFile = docFileRepository.findByFileIdAndLiteratureId(fileId, literature.getId()).orElse(new DocFile());
                    docFile.setFileId(fileId).setLiteratureId(literature.getId()).setBigDb(true);

                    GiveRecord giveRecord = new GiveRecord();
                    giveRecord.setFileId(fileId)
                            .setType(GiveTypeEnum.BIG_DB.value())
                            .setGiverName(GiveTypeEnum.BIG_DB.name())
                            .setStatus(GiveStatusEnum.SUCCESS.value());
                    giveRecord.setHelpRecordId(helpRecord.getId());


                    helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSED.value());
                    docFileRepository.save(docFile);
                    giveRecordRepository.save(giveRecord);
                    helpRecordRepository.updateStatus(helpRecord.getId(),HelpStatusEnum.HELP_SUCCESSED.value());
                } else {
                    flag[0] = false;
                }
            });
        } catch (Exception e) {
            log.info("pdf 服务平台正在调试");
            flag[0] = false;
            return flag[0];
        }
        return flag[0];
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
        helpRecordRepository.updateStatus(helpRecord.getId(),HelpStatusEnum.HELP_SUCCESSED.value());

        return true;
    }
}
