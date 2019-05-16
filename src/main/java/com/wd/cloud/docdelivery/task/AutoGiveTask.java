package com.wd.cloud.docdelivery.task;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.AppContextUtil;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.PdfSearchServerApi;
import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.repository.DocFileRepository;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: He Zhigang
 * @Date: 2019/4/17 15:04
 * @Description:
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AutoGiveTask implements Runnable {

    private GiveRecordRepository giveRecordRepository;
    private HelpRecordRepository helpRecordRepository;
    private LiteratureRepository literatureRepository;
    private DocFileRepository docFileRepository;
    private PdfSearchServerApi pdfSearchServerApi;
    private Long helpRecordId;

    public AutoGiveTask(Long helpRecordId) {
        this.giveRecordRepository = AppContextUtil.getBean(GiveRecordRepository.class);
        this.helpRecordRepository = AppContextUtil.getBean(HelpRecordRepository.class);
        this.literatureRepository = AppContextUtil.getBean(LiteratureRepository.class);
        this.docFileRepository = AppContextUtil.getBean(DocFileRepository.class);
        this.pdfSearchServerApi = AppContextUtil.getBean(PdfSearchServerApi.class);
        this.helpRecordId = helpRecordId;
    }

    /**
     * 执行自动应助
     */
    @Override
    public void run() {

        helpRecordRepository.findByIdAndStatusNot(helpRecordId, HelpStatusEnum.HELP_SUCCESSED.value()).ifPresent(helpRecord -> {
            DocFile reusingDocFile = docFileRepository.findByLiteratureIdAndReusingIsTrue(helpRecord.getLiteratureId());
            if (null != reusingDocFile) {
                reusingGive(reusingDocFile, helpRecord);
            } else {
                bigDbGive(helpRecord);
            }
        });
    }

    /**
     * 数据平台应助
     *
     * @param helpRecord
     */
    public void bigDbGive(HelpRecord helpRecord) {

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
                helpRecordRepository.save(helpRecord);
            }
        });
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
        helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSED.value());
        helpRecordRepository.save(helpRecord);
        return true;
    }


}
