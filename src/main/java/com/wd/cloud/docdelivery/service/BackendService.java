package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author He Zhigang
 * @date 2018/5/8
 * @Description:
 */
public interface BackendService {


    /**
     * 获取互助列表
     *
     * @return
     */
    Page<HelpRecordDTO> getHelpList(Integer status,String orgFlag,String keyword,String watchName,String beginTime,String endTime,Pageable pageable);

    Page<Literature> getLiteratureList(Pageable pageable, Map<String, Object> param);

    List<DocFile> getDocFileList(Pageable pageable, Long literatureId);

    DocFile saveDocFile(Long literatureId, String fileId);

    void give(Long id, String giverName, MultipartFile file);

    void third(Long id, String giverName);

    void failed(Long id, String giverName);

    void auditPass(Long id, String handlerName);

    void auditNoPass(Long id, String handlerName);

    /**
     * 获取单条可处理的记录
     *
     * @param id
     * @return
     */
    HelpRecord getWaitOrThirdHelpRecord(Long id);

    /**
     * 获取待审核的求助记录
     *
     * @param id
     * @return
     */
    HelpRecord getWaitAuditHelpRecord(Long id);

    /**
     * 复用、取消复用
     *
     * @return
     */
    void reusing(Long literatureId, Long docFileId, Boolean reusing, String handlerName);

}
