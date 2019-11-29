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

    /**
     *
     * @param id
     * @param helpRecordId1
     * @param invalid
     * @param gmtModified
     */
    @Modifying
    @Transactional(rollbackOn = Exception.class)
    @Query(value = "update help_raw set help_record_id = ?2,invalid = ?3 ,gmt_modified = ?4 where id = ?1", nativeQuery = true)
    void updateHelpRecordId(Long id, Long helpRecordId1, Integer invalid, Date gmtModified);

    /**
     * 渠道用户求助总量
     * @param helperName
     * @param channel
     * @return
     */
    long countByHelperNameAndHelpChannel(String helperName, Long channel);

    /**
     * 渠道用户求助总量
     * @param helperEmail
     * @param channel
     * @return
     */
    long countByHelperEmailAndHelpChannel(String helperEmail, Long channel);

    /**
     * 渠道用户今日求助总量
     * @param helperName
     * @param channel
     * @return
     */
    @Query(value = "select count(*) from help_raw where helper_name = ?1 and help_channel = ?2 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countByHelperNameToday(String helperName, Long channel);

    /**
     * 渠道用户今日求助总量
     * @param helperEmail
     * @param channel
     * @return
     */
    @Query(value = "select count(*) from help_raw where helper_email = ?1 and help_channel = ?2 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countByHelperEmailToday(String helperEmail, Long channel);
}