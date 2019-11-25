package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.dto.GiveRecordDTO;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @author He Zhigang
 * @date 2018/5/7
 * @Description:
 */
public interface FrontService {

    /**
     * 应助认领
     * @param helpRecordId
     * @param giverName
     * @param ip
     */
    void give(Long helpRecordId, String giverName, String ip);

    /**
     * 上传应助文件
     * @param helpRecord
     * @param giverName
     * @param file
     * @param ip
     */
    void uploadFile(HelpRecord helpRecord, String giverName, MultipartFile file, String ip);

    /**
     * 取消应助
     * @param helpRecordId
     * @param giverName
     */
    void cancelGivingHelp(long helpRecordId, String giverName);

    /**
     * 得到应种中状态的应助记录
     *
     * @param helpRecordId
     * @return
     */
    HelpRecord getHelpingRecord(long helpRecordId);

    /**
     * 获取用户今天的求助次数
     *
     * @param email
     * @return
     */
    Long getCountHelpRecordToDay(String email);


    /**
     * 设置文件url
     * @param helpRecord
     * @param giverName
     * @param fileId
     * @param giveIp
     */
    void setFile(HelpRecord helpRecord, String giverName, String fileId, String giveIp);

    /**
     * 我的求助记录
     * @param helperName
     * @param status
     * @param isDifficult
     * @param pageable
     * @return
     */
    Page<HelpRecordDTO> myHelpRecords(String helperName, List<Integer> status, Boolean isDifficult, List<Long> helpChannel, Pageable pageable);

    /**
     * 我的应助记录
     * @param giverName
     * @param status
     * @param pageable
     * @return
     */
    Page<GiveRecordDTO> myGiveRecords(String giverName, List<Integer> status, Pageable pageable);


    /**
     * 求助列表
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
    Page<HelpRecordDTO> getHelpRecords(List<Long> channel, List<Integer> status, String email, String keyword, Boolean isDifficult, String orgFlag, Date beginTime, Date endTime, Pageable pageable);

    /**
     * 待应助列表
     * @param channel
     * @param isDifficult
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    Page<HelpRecordDTO> getWaitHelpRecords(List<Long> channel, Boolean isDifficult, String orgFlag, Date beginTime, Date endTime, Pageable pageable);

    /**
     * 求助成功列表
     * @param helpChannel
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    Page<HelpRecordDTO> getSuccessHelpRecords(List<Long> helpChannel, String orgFlag, Date beginTime, Date endTime, Pageable pageable);

    /**
     * 疑难文献列表
     * @param helpChannel
     * @param orgFlag
     * @param beginTime
     * @param endTime
     * @param pageable
     * @return
     */
    Page<HelpRecordDTO> getDifficultHelpRecords(List<Long> helpChannel, String orgFlag, Date beginTime, Date endTime, Pageable pageable);

    /**
     * 求助权限
     * @param orgFlag
     * @param level
     * @param channel
     * @return
     */
    Permission getPermission(String orgFlag, Integer level, Long channel);

    /**
     * 下一级求助权限
     * @param orgFlag
     * @param level
     * @param channel
     * @return
     */
    Permission nextPermission(String orgFlag, Integer level, Long channel);

}
