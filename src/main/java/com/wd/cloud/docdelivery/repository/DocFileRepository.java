package com.wd.cloud.docdelivery.repository;

import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2018/5/27
 * @Description:
 */
public interface DocFileRepository extends JpaRepository<DocFile, Long> {

    List<DocFile> findByLiteratureIdOrderByReusingDescGmtModifiedDesc(Long literatureId);

    DocFile findByLiteratureIdAndReusingIsTrue(long literature);

    Optional<DocFile> findByLiteratureIdAndFileId(long literatureId, String fileId);

    /**
     * 查询文献所有的文档
     *
     * @param literatureId
     * @return
     */
    List<DocFile> findByLiteratureId(Long literatureId);

    List<DocFile> findByLiteratureIdAndBigDbFalse(Long literatureId);

    List<DocFile> findByLiteratureIdIn(List ids);

    Optional<DocFile> findByFileIdAndLiteratureId(String fileId, long literatureId);


}
