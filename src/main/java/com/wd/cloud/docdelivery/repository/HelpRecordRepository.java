package com.wd.cloud.docdelivery.repository;

import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author He Zhigang
 * @date 2018/5/7
 * @Description:
 */
public interface HelpRecordRepository extends JpaRepository<HelpRecord, Long>, JpaSpecificationExecutor<HelpRecord> {


    /**
     * 根据状态查询
     * @param status
     * @param date
     * @return
     */
    List<HelpRecord> findByStatusAndGmtModifiedBefore(Integer status, Date date);
    /**
     * 查询
     *
     * @param id
     * @param status
     * @return
     */
    Optional<HelpRecord> findByIdAndStatus(long id, int status);

    Optional<HelpRecord> findByIdAndStatusNot(long id, int status);

    /**
     * 我正在应助的
     * @param giverName
     * @param status
     * @return
     */
    Optional<HelpRecord> findByGiverNameAndStatus(String giverName,Integer status);

    Optional<HelpRecord> findByIdAndGiverNameAndStatus(Long id,String giverName,Integer status);



    HelpRecord findByIdAndStatusIn(long id, int[] status);

    /**
     * 统计一个机构某个时间内的求助数量
     *
     * @param orgName
     * @param createDate
     * @param format     date_format(date,"%Y-%m-%d %H:%i:%s")
     * @return
     */
    @Query(value = "select org_name as orgName, count(*) as ddcCount from help_record where org_name=?1 and date_format(gmt_create,?3) = date_format(?2,?3)", nativeQuery = true)
    List<Map<String, Object>> findByOrgNameDdcCount(String orgName, String createDate, String format);

    @Query(value = "select org_name as orgName, count(*) as ddcCount from help_record where date_format(gmt_create,?2) = date_format(?1,?2) group by org_name", nativeQuery = true)
    List<Map<String, Object>> findAllDdcCount(String createDate, String format);


    /**
     * 查询邮箱7天内求助某篇文献的记录
     *
     * @param helperEmail
     * @param literatureId
     * @return
     */
    @Query(value = "select * FROM help_record where helper_email = ?1 AND literature_id = ?2 AND 7 > TIMESTAMPDIFF(DAY, gmt_create, now())", nativeQuery = true)
    Optional<HelpRecord> findByHelperEmailAndLiteratureId(String helperEmail, Long literatureId);


    /**
     * 不同状态的总量
     *
     * @param status
     * @return
     */
    long countByStatus(Integer status);


    /**
     * 求助统计
     *
     * @return
     */
    @Query(value = "SELECT count(*) AS total,sum(IF (STATUS=4 or STATUS=3,1,0)) AS successTotal,sum(IF (TO_DAYS(gmt_create)=TO_DAYS(now()),1,0)) AS todayTotal,sum(IF (TO_DAYS(gmt_create)=TO_DAYS(now()) AND STATUS=4,1,0)) AS todaySuccessTotal FROM help_record", nativeQuery = true)
    Map<String, Long> tj();

    /**
     * 今日求助总量
     *
     * @return
     */
    @Query(value = "select count(*) from help_record where TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countToday();

    /**
     * 今日成功数量
     *
     * @return
     */
    @Query(value = "select count(*) from help_record where status = 4 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long successCountToday();

    @Query(value = "select count(*) as sumCount, sum(if (status=4,1,0)) as successCount from help_record where TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    Map<String, Object> totayTj();

    /**
     * 今日用户求助量
     *
     * @param helperName
     * @return
     */
    @Query(value = "select count(*) from help_record where helper_name = ?1 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countByHelperNameToday(String helperName);

    /**
     * @param helperName
     * @param channel
     * @return
     */
    @Query(value = "select count(*) from help_record where helper_name = ?1 and help_channel = ?2 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countByHelperNameToday(String helperName, Long channel);

    /**
     * 今日邮箱求助量
     *
     * @param email
     * @return
     */
    @Query(value = "select count(*) from help_record where helper_email = ?1 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countByHelperEmailToday(String email);

    /**
     * 今日邮箱求助量
     *
     * @param email
     * @param channel
     * @return
     */
    @Query(value = "select count(*) from help_record where helper_email = ?1 and help_channel = ?2 and TO_DAYS(gmt_create) = TO_DAYS(NOW())", nativeQuery = true)
    long countByHelperEmailToday(String email, Long channel);

    /**
     * 用户求助总量
     *
     * @param helperName
     * @return
     */
    long countByHelperName(String helperName);

    /**
     * 用户求助总量
     *
     * @param helperName
     * @param channel
     * @return
     */
    long countByHelperNameAndHelpChannel(String helperName, Long channel);

    /**
     * 邮箱求助总量
     *
     * @param helperEmail
     * @return
     */
    long countByHelperEmail(String helperEmail);

    /**
     * 邮箱求助总量
     *
     * @param helperEmail
     * @param channel
     * @return
     */
    long countByHelperEmailAndHelpChannel(String helperEmail, Long channel);
    /**
     * 统计某邮箱求助状态的数量
     *
     * @param helperEmail
     * @param status
     * @return
     */
    long countByHelperEmailAndStatus(String helperEmail, Integer status);


    /**
     * 统计某邮箱求助状态的数量
     *
     * @param helperEmail
     * @param channel
     * @param status
     * @return
     */
    long countByHelperEmailAndHelpChannelAndStatus(String helperEmail, Long channel, Integer status);
    /**
     * 统计某用户求助状态的数量
     *
     * @param helperName
     * @param status
     * @return
     */
    long countByHelperNameAndStatus(String helperName, Integer status);

    /**
     * 统计某用户求助状态的数量
     *
     * @param helperName
     * @param status
     * @param channel
     * @return
     */
    long countByHelperNameAndHelpChannelAndStatus(String helperName, Long channel, Integer status);

    /**
     * @param literatureId
     * @return
     */
    List<HelpRecord> findByLiteratureId(Long literatureId);

    /**
     *
     * @param ids
     * @return
     */
    List<HelpRecord> findByLiteratureIdIn(List ids);

    /**
     *
     * @param helperName
     * @return
     */
    HelpRecord findByHelperName(String helperName);

    @Query(value = "select avg(TIMESTAMPDIFF(HOUR,t1.gmt_create,t2.gmt_create)) from help_record t1,give_record t2 where t1.id = t2.help_record_id and t1.gmt_create >= ?1", nativeQuery = true)
    long avgResponseDate(String startDate);

    @Query(value = "select avg(TIMESTAMPDIFF(HOUR,gmt_create,gmt_modified)) from help_record t where t.status=4 and t.gmt_create >= ?1", nativeQuery = true)
    long avgSuccessResponseDate(String startDate);

}
