package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
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
    Page<HelpRecordDTO> getHelpList(Integer status, String orgFlag, String keyword, String watchName, List<Integer> giveType, Date beginTime, Date endTime, Pageable pageable);

    Page<Literature> getLiteratureList(Pageable pageable, Map<String, Object> param);

    /**
     * 获取文献所上传的全文列表
     * @param literatureId
     * @return
     */
    List<DocFile> getDocFileList(Long literatureId);

    /**
     * 保存docFile记录
     * @param literatureId
     * @param fileId
     * @return
     */
    DocFile saveDocFile(Long literatureId, String fileId);

    /**
     * 管理员上传文件直接处理
     * @param id
     * @param handlerName
     * @param file
     */
    void give(Long id, String handlerName, MultipartFile file);

    /**
     * 求助第三方
     * @param id
     * @param giverName
     */
    void third(Long id, String giverName);

    /**
     * 疑难文献
     * @param id
     * @param giverName
     */
    void difficult(Long id, String giverName);

    /**
     * 审核
     * @param id
     * @param handlerName
     * @param pass
     */
    void audit(Long id, String handlerName,Boolean pass);


    /**
     * 复用、取消复用
     *
     * @return
     */
    void reusing(Long literatureId, Long docFileId, Boolean reusing, String handlerName);

}
