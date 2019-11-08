package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.exception.FeignException;
import com.wd.cloud.commons.exception.NotFoundException;
import com.wd.cloud.commons.exception.UndefinedException;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.commons.util.FileUtil;
import com.wd.cloud.commons.util.StrUtil;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.enums.GiveStatusEnum;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.exception.ExceptionEnum;
import com.wd.cloud.docdelivery.feign.FsServerApi;
import com.wd.cloud.docdelivery.feign.SdolServerApi;
import com.wd.cloud.docdelivery.pojo.dto.GiveRecordDTO;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.*;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.FileService;
import com.wd.cloud.docdelivery.service.FrontService;
import com.wd.cloud.docdelivery.service.GiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * @author He Zhigang
 * @date 2018/5/7
 * @Description:
 */
@Slf4j
@Service("frontService")
@Transactional(rollbackFor = Exception.class)
public class FrontServiceImpl implements FrontService {

    @Autowired
    Global global;
    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    DocFileRepository docFileRepository;

    @Autowired
    FsServerApi fsServerApi;

    @Autowired
    SdolServerApi sdolServerApi;

    @Autowired
    GiveService giveService;

    @Autowired
    FileService fileService;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;


    @Override
    public void give(Long helpRecordId, String giverName, String ip) {
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(helpRecordId);

        optionalHelpRecord.ifPresent(helpRecord -> {
            // 该求助记录状态为应助中，表示已经被其它用户认领
            if (HelpStatusEnum.HELPING.value() == helpRecord.getStatus()) {
                throw new AppException(ExceptionEnum.GIVE_ING);
            }
            // 可认领待应助，求助第三方或疑难状态的求助
            if (helpRecord.getStatus() == HelpStatusEnum.HELP_THIRD.value()
                    || helpRecord.getStatus() == HelpStatusEnum.WAIT_HELP.value()
                    || helpRecord.getStatus() == HelpStatusEnum.HELP_FAILED.value()) {
                //检查用户是否已经认领了其它应助
                Optional<VHelpRecord> row = vHelpRecordRepository.findByGiverNameAndStatus(giverName, HelpStatusEnum.HELPING.value());
                if (row.isPresent()) {
                    throw new AppException(ExceptionEnum.GIVE_CLAIM.status(), "请先完成您正在应助的文献:" + row.get().getDocTitle());
                } else {
                    GiveRecord giveRecord = new GiveRecord();
                    giveRecord.setHelpRecordId(helpRecordId)
                            .setGiverIp(ip)
                            .setGiverName(giverName)
                            .setType(GiveTypeEnum.USER.value())
                            .setStatus(GiveStatusEnum.WAIT_UPLOAD.value());
                    // giverName认领应助成功
                    helpRecord.setStatus(HelpStatusEnum.HELPING.value()).setGiverName(giverName).setGiveType(GiveTypeEnum.USER.value());
                    //保存的同时，关联更新求助记录状态
                    giveRecordRepository.save(giveRecord);
                }
            } else {
                throw new NotFoundException();
            }
        });
    }

    @Override
    public void uploadFile(HelpRecord helpRecord, String giverName, MultipartFile file, String ip) {
        String fileId = null;
        try {
            String fileMd5 = FileUtil.fileMd5(file.getInputStream());
            ResponseModel<JSONObject> checkResult = fsServerApi.checkFile(global.getHbaseTableName(), fileMd5);
            if (!checkResult.isError() && checkResult.getBody() != null) {
                log.info("文件已存在，秒传成功！");
                fileId = checkResult.getBody().getStr("fileId");
            }
        } catch (IOException e) {
            throw new UndefinedException(e);
        }
        if (StrUtil.isBlank(fileId)) {
            //保存文件
            ResponseModel<JSONObject> responseModel = fsServerApi.uploadFile(global.getHbaseTableName(), file);
            if (responseModel.isError()) {
                log.error("文件服务调用失败：{}", responseModel.getMessage());
                throw new FeignException("fsServer.uploadFile");
            }
            fileId = responseModel.getBody().getStr("fileId");
        }
        //更新记录
        setFile(helpRecord, giverName, fileId, ip);


    }


    /**
     * 主动取消应助，删除应助记录
     *
     * @param helpRecordId
     * @param giverName
     * @return
     */
    @Override
    public void cancelGivingHelp(long helpRecordId, String giverName) {

        Optional<HelpRecord> row = helpRecordRepository.findByIdAndGiverNameAndStatus(helpRecordId, giverName, HelpStatusEnum.HELPING.value());
        row.ifPresent(helpRecord -> {
            helpRecord.setStatus(HelpStatusEnum.WAIT_HELP.value()).setGiverName(null).setGiveType(null);
            giveRecordRepository.deleteByGiverNameAndHelpRecordIdAndStatus(giverName, helpRecordId, GiveStatusEnum.WAIT_UPLOAD.value());
        });
    }

    @Override
    public HelpRecord getHelpingRecord(long helpRecordId) {
        return helpRecordRepository.findByIdAndStatus(helpRecordId, HelpStatusEnum.HELPING.value()).orElseThrow(NotFoundException::new);
    }

    @Override
    public Long getCountHelpRecordToDay(String email) {
        return helpRecordRepository.countByHelperEmailToday(email);
    }

    public HelpRecord getWaitOrThirdHelpRecord(Long id) {
        return helpRecordRepository.findByIdAndStatusIn(id,
                new int[]{HelpStatusEnum.WAIT_HELP.value(), HelpStatusEnum.HELP_THIRD.value()});
    }

    public String clearHtml(String docTitle) {
        return HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(docTitle));
    }

    @Override
    public void setFile(HelpRecord helpRecord, String giverName, String fileId, String giveIp) {
        //更新求助状态为待审核
        helpRecord.setStatus(HelpStatusEnum.WAIT_AUDIT.value()).setFileId(fileId);
        GiveRecord giveRecord = giveRecordRepository.findByHelpRecordIdAndStatusAndGiverName(helpRecord.getId(), GiveStatusEnum.WAIT_UPLOAD.value(), giverName);
        //关联应助记录
        giveRecord.setFileId(fileId)
                .setGiverName(giverName)
                .setGiverIp(giveIp)
                //前台上传的，需要后台人员再审核
                .setStatus(GiveStatusEnum.WAIT_AUDIT.value());

    }

    @Override
    public Page<HelpRecordDTO> myHelpRecords(String username, List<Integer> status, Boolean isDifficult, List<Long> helpChannel, Pageable pageable) {
        Page<VHelpRecord> vHelpRecords = vHelpRecordRepository.findAll(VHelpRecordRepository.SpecBuilder.buildVhelpRecord(helpChannel, status, null, username, null, isDifficult, null, null, null), pageable);
        return coversHelpRecordDTO(vHelpRecords);
    }

    /**
     * 我的应助记录
     *
     * @param giverName
     * @param status
     * @param pageable
     * @return
     */
    @Override
    public Page<GiveRecordDTO> myGiveRecords(String giverName, List<Integer> status, Pageable pageable) {
        Page<GiveRecord> giveRecords = giveRecordRepository.findAll(GiveRecordRepository.SpecBuilder.buildGiveRecord(status, giverName), pageable);
        return coversGiveRecordDTO(giveRecords);
    }


    /**
     * 求助列表
     *
     * @param channel
     * @param status
     * @param email
     * @param keyword
     * @param isDifficult
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    @Override
    public Page<HelpRecordDTO> getHelpRecords(List<Long> channel, List<Integer> status, String email, String keyword, Boolean isDifficult, String orgFlag, Date beginTime, Date endTime, Pageable pageable) {
        Date end = endTime == null ? new Date() : endTime;
        // 默认只返回最近一个星期的数据
        Date begin = beginTime == null ? DateUtil.offsetWeek(end, -1).toJdkDate() : beginTime;
        Page<VHelpRecord> vHelpRecords = vHelpRecordRepository.findAll(VHelpRecordRepository.SpecBuilder.buildVhelpRecord(channel, status, email, null, keyword, isDifficult, orgFlag, begin, end), pageable);
        return coversHelpRecordDTO(vHelpRecords);
    }

    /**
     * 待应助列表
     *
     * @param channel
     * @param isDifficult
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    @Override
    public Page<HelpRecordDTO> getWaitHelpRecords(List<Long> channel, Boolean isDifficult, String orgFlag, Date beginTime, Date endTime, Pageable pageable) {
        // 待应助记录包含三种状态，0,1,3
        List<Integer> status = CollectionUtil.newArrayList(
                HelpStatusEnum.WAIT_HELP.value(),
                HelpStatusEnum.HELPING.value(),
                HelpStatusEnum.HELP_THIRD.value());
        Date end = endTime == null ? new Date() : endTime;
        // 默认只返回最近一个星期的数据
        Date begin = beginTime == null ? DateUtil.offsetWeek(end, -1).toJdkDate() : beginTime;
        Page<VHelpRecord> waitHelpRecords = vHelpRecordRepository.findAll(VHelpRecordRepository.SpecBuilder.buildVhelpRecord(channel, status, null, null, null, isDifficult, orgFlag, begin, end), pageable);
        return coversHelpRecordDTO(waitHelpRecords);
    }

    /**
     * 求助成功列表
     *
     * @param helpChannel
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    @Override
    public Page<HelpRecordDTO> getSuccessHelpRecords(List<Long> helpChannel, String orgFlag, Date beginTime, Date endTime, Pageable pageable) {
        List<Integer> status = CollectionUtil.newArrayList(HelpStatusEnum.HELP_SUCCESSED.value());
        Date end = endTime == null ? new Date() : endTime;
        // 默认只返回最近一个星期的数据
        Date begin = beginTime == null ? DateUtil.offsetWeek(end, -1).toJdkDate() : beginTime;
        Page<VHelpRecord> finishHelpRecords = vHelpRecordRepository.findAll(VHelpRecordRepository.SpecBuilder.buildVhelpRecord(helpChannel, status, null, null, null, null, orgFlag, begin, end), pageable);
        return coversHelpRecordDTO(finishHelpRecords);
    }

    /**
     * 疑难文献列表
     * @param channel
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    @Override
    public Page<HelpRecordDTO> getDifficultHelpRecords(List<Long> channel, String orgFlag, Date beginTime, Date endTime, Pageable pageable) {
        // start 需求(【互助大厅-疑难文献取值范围修改】
        //https://www.tapd.cn/47850539/prong/stories/view/1147850539001000859)
        Date endDate = endTime != null ? endTime : new Date();
        // 默认一周间隔
        Date beginDate = beginTime != null ? beginTime : DateUtil.offsetWeek(endDate, -1);
        // end
        Page<VHelpRecord> finishHelpRecords = vHelpRecordRepository.findAll(VHelpRecordRepository.SpecBuilder.buildVhelpRecord(channel, null, null, null, null, true, orgFlag, beginDate, endDate), pageable);
        return coversHelpRecordDTO(finishHelpRecords);
    }


    @Override
    public Permission nextPermission(String orgFlag, Integer level) {
        int nextLevel = nexLevel(level);
        return getPermission(orgFlag, nextLevel);
    }

    @Override
    public Permission getPermission(String orgFlag, Integer level) {
        Permission permission = null;
        if (orgFlag != null) {
            permission = permissionRepository.getOrgFlagAndLevel(orgFlag, level);
        }
        if (permission == null) {
            permission = permissionRepository.findByOrgFlagIsNullAndLevel(level);
        }
        return permission;
    }

    private int nexLevel(int level) {
        switch (level) {
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 6;
            case 3:
                return 7;
            default:
                return 0;
        }
    }


    private Page<HelpRecordDTO> coversHelpRecordDTO(Page<VHelpRecord> helpRecordPage) {
        return helpRecordPage.map(vHelpRecord -> {
            HelpRecordDTO helpRecordDTO = BeanUtil.toBean(anonymous(vHelpRecord), HelpRecordDTO.class);
            Optional<Literature> optionalLiterature = literatureRepository.findById(vHelpRecord.getLiteratureId());
            optionalLiterature.ifPresent(literature -> helpRecordDTO.setDocTitle(literature.getDocTitle()).setDocHref(literature.getDocHref()));
//            //如果有用户正在应助
//            if (vHelpRecord.getStatus() == HelpStatusEnum.HELPING.value()) {
//                Optional<GiveRecord> optionalGiveRecord = giveService.getGiveRecord(vHelpRecord.getId(), GiveStatusEnum.WAIT_UPLOAD);
//                optionalGiveRecord.ifPresent(helpRecordDTO::setGiving);
//            }
            return helpRecordDTO;
        });
    }

    private Page<GiveRecordDTO> coversGiveRecordDTO(Page<GiveRecord> giveRecordPage) {
        return giveRecordPage.map(giveRecord -> {
            GiveRecordDTO giveRecordDTO = BeanUtil.toBean(giveRecord, GiveRecordDTO.class);
            Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(giveRecord.getHelpRecordId());
            optionalHelpRecord.ifPresent(helpRecord -> {
                giveRecordDTO.setHelperEmail(BooleanUtil.isTrue(helpRecord.getAnonymous()) ? "匿名" : StrUtil.hideMailAddr(helpRecord.getHelperEmail()))
                        .setRemark(helpRecord.getRemark()).setOrgName(helpRecord.getOrgName());
                Optional<Literature> optionalLiterature = literatureRepository.findById(helpRecord.getLiteratureId());
                optionalLiterature.ifPresent(literature -> {
                    giveRecordDTO.setDocTitle(literature.getDocTitle()).setDocHref(literature.getDocHref());
                });
            });
            return giveRecordDTO;
        });
    }

    /**
     * 匿名和邮箱隐藏处理
     *
     * @param vHelpRecord
     * @return
     */
    private VHelpRecord anonymous(VHelpRecord vHelpRecord) {
        if (BooleanUtil.isTrue(vHelpRecord.getAnonymous())) {
            vHelpRecord.setHelperEmail("匿名").setHelperName("匿名");
        } else {
            String helperEmail = vHelpRecord.getHelperEmail();
            String s = StrUtil.hideMailAddr(helperEmail);
            vHelpRecord.setHelperEmail(s);
        }
        return vHelpRecord;
    }


}
