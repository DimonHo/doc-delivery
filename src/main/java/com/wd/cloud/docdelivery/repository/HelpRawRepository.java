package com.wd.cloud.docdelivery.repository;

import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @author Hu Langshi
 * @date 2019/10/14
 * @Description:
 */
public interface HelpRawRepository extends JpaRepository<HelpRaw,Long>, JpaSpecificationExecutor<HelpRaw> {

    @Modifying
    @Transactional
    @Query(value = "update help_raw set help_record_id = ?2,invalid = ?3 ,gmt_modified = ?4 where id = ?1", nativeQuery = true)
    void updateHelpRecordId(Long id, Long helpRecordId, Integer invalid, Date gmtModified);
}