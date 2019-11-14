package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.exception.FeignException;
import com.wd.cloud.commons.exception.NotFoundException;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.commons.util.FileUtil;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.exception.ExceptionEnum;
import com.wd.cloud.docdelivery.feign.FsServerApi;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.dto.LiteratureDTO;
import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.GiveService;
import com.wd.cloud.docdelivery.service.MailService;
import com.wd.cloud.docdelivery.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;

/**
 * @author He Zhigang
 * @date 2018/12/26
 * @Description:
 */
@Slf4j
@Service("processService")
@Transactional(rollbackFor = Exception.class)
public class ProcessServiceImpl implements ProcessService {

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
    GiveService giveService;

    @Autowired
    MailService mailService;

    @Autowired
    Global global;

    @Override
    public void third(Long helpRecordId, String handlerName) {
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(helpRecordId);
        // 如果不是待应助状态，中断操作
        if (optionalHelpRecord.isPresent()) {
            HelpRecord helpRecord = optionalHelpRecord.get();
            if (helpRecord.getStatus() != HelpStatusEnum.WAIT_HELP.value()) {
                throw new AppException(HttpStatus.HTTP_INTERNAL_ERROR, "非法操作");
            }
            helpRecord.setStatus(HelpStatusEnum.HELP_THIRD.value());
            GiveRecord giveRecord = new GiveRecord();
            giveRecord.setHelpRecordId(helpRecord.getId())
                    .setHandlerName(handlerName)
                    .setType(GiveTypeEnum.MANAGER.value());
            giveRecordRepository.save(giveRecord);
            helpRecordRepository.save(helpRecord);
            Optional<VHelpRecord> optionalVHelpRecord = vHelpRecordRepository.findById(helpRecordId);
            optionalVHelpRecord.ifPresent(vHelpRecord -> mailService.sendMail(vHelpRecord));
        } else {
            throw new NotFoundException("没有找到ID为【" + helpRecordId + "】的求助记录");
        }
    }

    @Override
    public void give(Long helpRecordId, String handlerName, MultipartFile file) {
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(helpRecordId);
        if (optionalHelpRecord.isPresent()) {
            HelpRecord helpRecord = optionalHelpRecord.get();
            if (helpRecord.getStatus() >= HelpStatusEnum.HELP_SUCCESSED.value()) {
                throw new AppException(ExceptionEnum.FLOW_STATUS);
            }
            // 获取fileId
            String fileId = getFileId(file);
            // 没有则上传文件
            fileId = fileId == null ? uploadFile(file) : fileId;
            DocFile docFile = new DocFile();
            docFile.setLiteratureId(helpRecord.getLiteratureId()).setFileId(fileId);

            //如果有求助第三方的状态的应助记录，则直接处理更新这个记录
            GiveRecord giveRecord = giveService.getGiveRecord(helpRecord.getId(), GiveStatusEnum.THIRD).orElse(new GiveRecord());
            giveRecord.setHelpRecordId(helpRecord.getId())
                    .setFileId(fileId)
                    .setType(GiveTypeEnum.MANAGER.value())
                    .setStatus(GiveStatusEnum.SUCCESS.value())
                    .setHandlerName(handlerName);
            //修改求助状态为应助成功
            helpRecord.setStatus(HelpStatusEnum.HELP_SUCCESSING.value());
            docFileRepository.save(docFile);
            giveRecordRepository.save(giveRecord);
            helpRecordRepository.save(helpRecord);
            Optional<VHelpRecord> optionalVHelpRecord = vHelpRecordRepository.findById(helpRecordId);
            optionalVHelpRecord.ifPresent(vHelpRecord -> mailService.sendMail(vHelpRecord));
        } else {
            throw new NotFoundException("没有找到ID为【" + helpRecordId + "】的求助记录");
        }
    }

    @Override
    public void markDifficult(Long helpRecordId, String handlerName) {
        HelpRecord helpRecord = helpRecordRepository.findById(helpRecordId).orElseThrow(NotFoundException::new);
        //标记为疑难文献
        helpRecord.setDifficult(true)
                // 状态回到待应助的状态
                .setStatus(HelpStatusEnum.WAIT_HELP.value())
                .setHandlerName(handlerName)
                .setGiveType(GiveTypeEnum.MANAGER.value())
                .setGiverName(null);
    }

    @Override
    public Page<HelpRecordDTO> waitHelpRecordList(Pageable pageable) {
        List<Integer> waitStatusList = CollectionUtil.newArrayList(HelpStatusEnum.WAIT_HELP.value(), HelpStatusEnum.HELP_THIRD.value());
        Map<String, Object> param = MapUtil.of("status", waitStatusList);
        return helpRecordList(param, pageable);
    }

    @Override
    public Page<HelpRecordDTO> successHelpRecordList(Pageable pageable) {
        List<Integer> waitStatusList = CollectionUtil.newArrayList(HelpStatusEnum.HELP_SUCCESSED.value());
        Map<String, Object> param = MapUtil.of("status", waitStatusList);
        return helpRecordList(param, pageable);
    }

    @Override
    public Page<HelpRecordDTO> waitAuditHelpRecordList(Pageable pageable) {
        List<Integer> waitStatusList = CollectionUtil.newArrayList(HelpStatusEnum.WAIT_AUDIT.value());
        Map<String, Object> param = MapUtil.of("status", waitStatusList);
        return helpRecordList(param, pageable);
    }

    @Override
    public Page<HelpRecordDTO> helpingHelpRecordList(Pageable pageable) {
        List<Integer> waitStatusList = CollectionUtil.newArrayList(HelpStatusEnum.HELPING.value());
        Map<String, Object> param = MapUtil.of("status", waitStatusList);
        return helpRecordList(param, pageable);
    }

    @Override
    public Page<HelpRecordDTO> helpRecordList(Map<String, Object> param, Pageable pageable) {
        String orgFlag = MapUtil.getStr(param, "orgFlag");
        List<Integer> statusList = MapUtil.get(param, "status", List.class);
        String keyword = MapUtil.getStr(param, "keyword") == null ? null : MapUtil.getStr(param, "keyword").replaceAll("\\\\", "\\\\\\\\");
        Date beginTime = MapUtil.getDate(param, "beginTime");
        Date endTime = DateUtil.endOfDay(MapUtil.getDate(param, "endTime"));
        Long helperId = MapUtil.getLong(param, "helperId");
        Page<VHelpRecord> result = vHelpRecordRepository.findAll(new Specification<VHelpRecord>() {
            @Override
            public Predicate toPredicate(Root<VHelpRecord> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                // 机构过滤
                if (StrUtil.isNotBlank(orgFlag)) {
                    list.add(cb.equal(root.get("orgFlag"), orgFlag));
                }
                if (helperId != null) {
                    list.add(cb.equal(root.get("helperId"), helperId));
                }
                // 状态过滤
                if (statusList != null && statusList.size() > 0) {
                    CriteriaBuilder.In<Integer> inStatus = cb.in(root.get("status"));
                    statusList.forEach(inStatus::value);
                    list.add(inStatus);
                }
                // 时间范围过滤
                if (beginTime != null) {
                    list.add(cb.between(root.get("gmtCreate").as(Date.class), beginTime, endTime));
                }
                // 模糊查询文献标题或邮箱
                if (!StringUtils.isEmpty(keyword)) {
                    list.add(cb.or(cb.like(root.get("docTitle").as(String.class), "%" + keyword.trim() + "%"),
                            cb.like(root.get("helperEmail").as(String.class), "%" + keyword.trim() + "%")));
                }
                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            }
        }, pageable);

        return result.map(vHelpRecord -> BeanUtil.toBean(vHelpRecord, HelpRecordDTO.class));
    }

    @Override
    public Page<LiteratureDTO> literatureList(Map<String, Object> query, Pageable pageable) {
        return null;
    }


    private String uploadFile(MultipartFile file) {
        ResponseModel<JSONObject> uploadResult = fsServerApi.uploadFile(global.getHbaseTableName(), file);
        if (uploadResult.isError()) {
            log.info("文件[file = {},size = {}] 上传失败 。。。", file.getOriginalFilename(), file.getSize());
            throw new FeignException("fsServer.uploadFile");
        }
        log.info("文件{}上传成功!", file.getOriginalFilename());
        return uploadResult.getBody().getStr("fileId");
    }

    /**
     * 检查文件是否已存在于文件服务器,如果存在，得到fileId
     *
     * @param file
     * @return
     */
    private String getFileId(MultipartFile file) {
        try {
            String fileMd5 = FileUtil.fileMd5(file.getInputStream());
            ResponseModel<JSONObject> checkResult = fsServerApi.checkFile(global.getHbaseTableName(), fileMd5);
            if (!checkResult.isError() && checkResult.getBody() != null) {
                log.info("文件已存在，秒传成功！");
                return checkResult.getBody().getStr("fileId");
            }
        } catch (IOException e) {
            throw new AppException(HttpStatus.HTTP_INTERNAL_ERROR, e.getMessage());
        }
        return null;
    }

}
