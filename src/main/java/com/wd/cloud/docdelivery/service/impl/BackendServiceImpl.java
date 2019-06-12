package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.exception.FeignException;
import com.wd.cloud.commons.exception.NotFoundException;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.commons.util.FileUtil;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.FsServerApi;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.*;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.BackendService;
import com.wd.cloud.docdelivery.service.FileService;
import com.wd.cloud.docdelivery.service.GiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author He Zhigang
 * @date 2018/5/8
 * @Description:
 */
@Slf4j
@Service("backendService")
public class BackendServiceImpl implements BackendService {

    @Autowired
    Global global;

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    DocFileRepository docFileRepository;

    @Autowired
    FsServerApi fsServerApi;

    @Autowired
    FileService fileService;

    @Autowired
    GiveService giveService;

    @Autowired
    ReusingLogRepository reusingLogRepository;


    @Override
    public Page<HelpRecordDTO> getHelpList(Integer status,String orgFlag,String keyword,String watchName,String beginTime,String endTime,Pageable pageable) {

        //  https://www.tapd.cn/33969136/bugtrace/bugs/view?bug_id=1133969136001000485
        keyword = keyword != null ? keyword.replaceAll("\\\\", "\\\\\\\\") : null;

        //根据条件查询视图
        Page<VHelpRecord> result = vHelpRecordRepository.findAll(VHelpRecordRepository.SpecBuilder.buildBackendList(orgFlag, status, keyword, beginTime, endTime, watchName), pageable);

        Page<HelpRecordDTO> helpRecordDTOS = result.map(vHelpRecord -> {
            HelpRecordDTO helpRecordDTO = BeanUtil.toBean(vHelpRecord, HelpRecordDTO.class);
            helpRecordDTO.setGiveRecords(giveRecordRepository.findByHelpRecordIdOrderByGmtModifiedDesc(vHelpRecord.getId()));
            return helpRecordDTO;
        });
        return helpRecordDTOS;
    }

    @Override
    public Page<Literature> getLiteratureList(Pageable pageable, Map<String, Object> param) {
        Boolean reusing = (Boolean) param.get("reusing");
        String keyword = ((String) param.get("keyword"));
        keyword = keyword != null ? keyword.replaceAll("\\\\", "\\\\\\\\") : null;
        return literatureRepository.findAll(LiteratureRepository.SpecBuilder.buildWaitResuing(reusing, keyword), pageable);
    }

    @Override
    public List<DocFile> getDocFileList(Pageable pageable, Long literatureId) {
        return docFileRepository.findByLiteratureIdAndBigDbFalse(literatureId);
    }

    @Override
    public DocFile saveDocFile(Long literatureId, String fileId) {
        Optional<DocFile> optionalDocFile = docFileRepository.findByLiteratureIdAndFileId(literatureId, fileId);
        if (!optionalDocFile.isPresent()) {
            DocFile docFile = new DocFile();
            docFile.setFileId(fileId);
            docFile.setLiteratureId(literatureId);
            docFile = docFileRepository.save(docFile);
            return docFile;
        }
        return optionalDocFile.get();
    }

    @Override
    public void give(Long helpRecordId, String giverName, MultipartFile file) {
        HelpRecord helpRecord = helpRecordRepository.findById(helpRecordId).orElseThrow(NotFoundException::new);
        log.info("正在上传文件[file = {},size = {}]", file.getOriginalFilename(), file.getSize());
        String fileMd5 = null;
        String fileId = null;
        try {
            fileMd5 = FileUtil.fileMd5(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponseModel<JSONObject> checkResult = fsServerApi.checkFile(global.getHbaseTableName(), fileMd5);
        if (!checkResult.isError() && checkResult.getBody() != null) {
            log.info("文件已存在，秒传成功！");
            fileId = checkResult.getBody().getStr("fileId");
        }
        if (fileId == null) {
            ResponseModel<JSONObject> uploadResult = fsServerApi.uploadFile(global.getHbaseTableName(), file);
            if (uploadResult.isError()) {
                log.error("文件服务调用失败：{}", uploadResult.getMessage());
                log.info("文件[file = {},size = {}] 上传失败 。。。", file.getOriginalFilename(), file.getSize());
                throw new FeignException("fsServer.uploadFile");
            }
            log.info("文件{}上传成功!", file.getOriginalFilename());
            fileId = uploadResult.getBody().getStr("fileId");
        }
        DocFile docFile = saveDocFile(helpRecord.getLiteratureId(), fileId);
        //如果有求助第三方的状态的应助记录，则直接处理更新这个记录
        GiveRecord giveRecord = giveService.getGiveRecord(helpRecord.getId(), GiveStatusEnum.THIRD).orElse(new GiveRecord());
        //如果没有第三方状态的记录，则新建一条应助记录
        giveRecord.setStatus(GiveStatusEnum.SUCCESS.value());
        giveRecord.setHelpRecordId(helpRecord.getId());
        giveRecord.setFileId(docFile.getFileId());
        //设置应助类型为管理员应助
        giveRecord.setType(GiveTypeEnum.MANAGER.value());
        giveRecord.setGiverName(giverName);
        //修改求助状态为应助成功
        helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSED.value());
        giveRecordRepository.save(giveRecord);
        helpRecordRepository.save(helpRecord);
    }


    @Override
    public void third(Long helpRecordId, String giverName) {
        HelpRecord helpRecord = helpRecordRepository.findByIdAndStatus(helpRecordId, HelpStatusEnum.WAIT_HELP.value());
        if (helpRecord == null) {
            throw new NotFoundException("没有找到待求助记录");
        }
        helpRecord.setStatus(HelpStatusEnum.HELP_THIRD.value());
        GiveRecord giveRecord = new GiveRecord();
        giveRecord.setType(GiveTypeEnum.MANAGER.value())
                .setStatus(GiveStatusEnum.THIRD.value())
                .setGiverName(giverName)
                .setHandlerName(giverName)
                .setHelpRecordId(helpRecord.getId());
        giveRecordRepository.save(giveRecord);
        helpRecordRepository.save(helpRecord);
    }

    @Override
    public void failed(Long helpRecordId, String giverName) {
        HelpRecord helpRecord = helpRecordRepository.findById(helpRecordId).orElseThrow(NotFoundException::new);
        //标记为疑难文献
        helpRecord.setStatus(HelpStatusEnum.HELP_FAILED.value()).setDifficult(true);

        //如果没有第三方状态的记录，则新建一条应助记录
        GiveRecord giveRecord = giveService.getGiveRecord(helpRecord.getId(), GiveStatusEnum.THIRD).orElse(new GiveRecord());
        giveRecord.setType(GiveTypeEnum.MANAGER.value())
                .setStatus(GiveStatusEnum.NO_RESULT.value())
                .setGiverName(giverName).setHandlerName(giverName)
                .setHelpRecordId(helpRecordId);
        giveRecordRepository.save(giveRecord);
        helpRecordRepository.save(helpRecord);
    }

    @Override
    public void auditPass(Long helpRecordId, String handlerName) {
        HelpRecord helpRecord = getWaitAuditHelpRecord(helpRecordId);
        // 查询待审核，且应助者为平台用户的应助记录
        GiveRecord giveRecord = giveRecordRepository
                .findByHelpRecordIdAndStatusAndType(helpRecordId, GiveStatusEnum.WAIT_AUDIT.value(), GiveTypeEnum.USER.value())
                .orElseThrow(NotFoundException::new);

        giveRecord.setStatus(GiveStatusEnum.SUCCESS.value());
        giveRecord.setHandlerName(handlerName);
        Literature literature = literatureRepository.findById(helpRecord.getLiteratureId()).orElseThrow(NotFoundException::new);

        DocFile docFile = docFileRepository.findByFileIdAndLiteratureId(giveRecord.getFileId(), literature.getId()).orElse(new DocFile());
        docFile.setLiteratureId(literature.getId()).setFileId(giveRecord.getFileId());

        helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSED.value()).setDifficult(false);
        docFileRepository.save(docFile);
        giveRecordRepository.save(giveRecord);
        helpRecordRepository.save(helpRecord);
    }

    @Override
    public void auditNoPass(Long helpRecordId, String handlerName) {
        HelpRecord helpRecord = getWaitAuditHelpRecord(helpRecordId);
        GiveRecord giveRecord = giveRecordRepository
                .findByHelpRecordIdAndStatusAndType(helpRecordId, GiveStatusEnum.WAIT_AUDIT.value(), GiveTypeEnum.USER.value())
                .orElseThrow(NotFoundException::new);
        giveRecord.setStatus(GiveStatusEnum.AUDIT_NO_PASS.value()).setHandlerName(handlerName);
        helpRecord.setStatus(BooleanUtil.isTrue(helpRecord.getDifficult()) ? HelpStatusEnum.HELP_FAILED.value() : HelpStatusEnum.WAIT_HELP.value());
        giveRecordRepository.save(giveRecord);
        helpRecordRepository.save(helpRecord);
    }

    @Override
    public HelpRecord getWaitOrThirdHelpRecord(Long id) {
        return helpRecordRepository.findByIdAndStatusIn(id,
                new int[]{HelpStatusEnum.WAIT_HELP.value(), HelpStatusEnum.HELP_THIRD.value(), HelpStatusEnum.HELP_FAILED.value()});
    }

    @Override
    public HelpRecord getWaitAuditHelpRecord(Long id) {
        return helpRecordRepository.findByIdAndStatus(id, HelpStatusEnum.WAIT_AUDIT.value());
    }

    @Override
    public void reusing(Long literatureId, Long docFileId, Boolean reusing, String handlerName) {
        Literature literature = literatureRepository.findById(literatureId).orElseThrow(NotFoundException::new);
        List<DocFile> docFiles = docFileRepository.findByLiteratureId(literatureId);
        docFiles.forEach(docFile -> {
            docFile.setReusing(false);
            if (docFile.getId().equals(docFileId)) {
                docFile.setReusing(reusing).setHandlerName(handlerName);
            }
        });
        docFileRepository.saveAll(docFiles);
        literature.setReusing(reusing).setLastHandlerName(handlerName);
        literatureRepository.save(literature);
    }


}
