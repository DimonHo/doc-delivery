package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDto;
import com.wd.cloud.docdelivery.pojo.dto.LiteratureDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author He Zhigang
 * @date 2018/12/26
 * @Description:
 */
public interface ProcessService {

    /**
     * 等待第三方应助
     *
     * @param helpRecordId
     * @param giverName
     */
    void third(Long helpRecordId, String giverName);

    /**
     * 直接应助成功
     *
     * @param helpRecordId
     * @param giverName
     * @param file
     */
    void give(Long helpRecordId, String giverName, MultipartFile file);

    /**
     * 标记疑难文献
     *
     * @param helpRecordId
     * @param giverName
     */
    void markDifficult(Long helpRecordId, String giverName);

    Page<HelpRecordDto> waitHelpRecordList(Pageable pageable);

    Page<HelpRecordDto> successHelpRecordList(Pageable pageable);

    Page<HelpRecordDto> waitAuditHelpRecordList(Pageable pageable);

    Page<HelpRecordDto> helpingHelpRecordList(Pageable pageable);

    Page<HelpRecordDto> helpRecordList(Map<String, Object> query, Pageable pageable);

    Page<LiteratureDto> literatureList(Map<String, Object> query, Pageable pageable);
}
