package com.wd.cloud.docdelivery.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.wd.cloud.docdelivery.enums.GiveTypeEnum;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.Predicate;
import java.util.*;

public interface VHelpRecordRepository extends JpaRepository<VHelpRecord, Long>, JpaSpecificationExecutor<VHelpRecord> {

    @Query(value = "select * from v_help_record where is_send = ?1 and gmt_modified <= ?2 and (status in ?3 or is_difficult = ?4)", nativeQuery = true)
    List<VHelpRecord> findBySendAndGmtModifiedBeforeAndStatusInOrDifficult(boolean isSend,Date gmtModified,List<Integer> status,boolean difficult);

    //@Query(value = "SELECT t1.help_date AS help_date,IFNULL(t2.total,0) AS total FROM (SELECT @s :=@s+1 AS _index,DATE_FORMAT(DATE_SUB(?4,INTERVAL @s HOUR),?2) AS help_date FROM mysql.help_topic,(SELECT @s :=-1) temp WHERE DATE(DATE_SUB(?4,INTERVAL @s HOUR))>=?3) AS t1 LEFT JOIN (SELECT count(id) AS total,date_format(gmt_create,?2) help_date FROM v_help_record WHERE org_flag=?1 AND gmt_create BETWEEN ?3 AND ?4 GROUP BY help_date) AS t2 ON t1.help_date=t2.help_date ORDER BY t1.help_date",nativeQuery = true)
    @Query(value = "select gmt_create from v_help_record group by gmt_create", nativeQuery = true)
    List<Map<String, Object>> orgTj(String orgFlag, String dateFormat, Date begin, Date end);

    @Query(value = "select * from v_help_record where status in (-1,0,1,3) and ((gmt_create between ?1 and ?3 and is_difficult = 0) or (is_difficult = 1 and gmt_create between ?2 and ?3))", nativeQuery = true)
    Page<VHelpRecord> findByWaitHelp(Date begin1, Date begin2, Date end, Pageable pageable);

    @Query(value = "select * from v_help_record where org_flag = ?4 and status in (-1,0,1,3) and ((gmt_create between ?1 and ?3 and is_difficult = 0) or (is_difficult = 1 and gmt_create between ?2 and ?3))", nativeQuery = true)
    Page<VHelpRecord> findByWaitHelpForOrg(Date begin1, Date begin2, Date end, String orgFlag, Pageable pageable);

    /**
     * 查询用户正在应助的文献
     * @param giverName
     * @param status
     * @return
     */
    Optional<VHelpRecord> findByGiverNameAndStatus(String giverName, Integer status);

    class SpecBuilder {

        public static Specification<VHelpRecord> buildBackendList(String orgFlag, List<Integer> status,Boolean isDifficult, String keyword, List<Integer> giveType, Date beginTime, Date endTime, String watchName) {
            return (Specification<VHelpRecord>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<>();

                if (isDifficult != null){
                    list.add(cb.equal(root.get("difficult"),isDifficult));
                }
                if (StrUtil.isNotBlank(orgFlag)) {
                    list.add(cb.equal(root.get("orgFlag"), orgFlag));
                }
                if (CollectionUtil.isNotEmpty(status)){
                    list.add(cb.in(root.get("status")).value(status));
                }

                if (CollectionUtil.isNotEmpty(giveType)) {
                    list.add(cb.in(root.get("giveType")).value(giveType));
                }else{

                    list.add(cb.or(
                            cb.and(cb.notEqual(root.get("giveType"), GiveTypeEnum.AUTO.value()),
                                    cb.notEqual(root.get("giveType"), GiveTypeEnum.BIG_DB.value())
                            ),
                            cb.isNull(root.get("giveType"))));
                }
                if (StrUtil.isNotBlank(keyword)) {
                    list.add(cb.or(
                            cb.like(root.get("docTitle").as(String.class), "%" + keyword.trim() + "%"),
                            cb.like(root.get("helperEmail").as(String.class), "%" + keyword.trim() + "%")
                            )
                    );
                }
                if (StrUtil.isNotBlank(watchName)) {
                    list.add(cb.equal(root.get("watchName"), watchName));
                }
                if (beginTime != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("gmtCreate").as(Date.class), beginTime));
                }
                // 默认最新返回10秒之前的求助，防止自动应助任务还未跑完，被文献传递人员抢先处理
                Date end = endTime != null ? endTime : DateUtil.offsetSecond(new Date(), -10).toJdkDate();
                list.add(cb.lessThanOrEqualTo(root.get("gmtCreate").as(Date.class), end));

                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }


        public static Specification<VHelpRecord> buildVhelpRecord(List<Long> channel, List<Integer> status, String email, String helperName, String keyword, Boolean isDifficult, String orgFlag, Date beginDate, Date endDate) {
            return (Specification<VHelpRecord>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<>();
                if (StrUtil.isNotBlank(orgFlag)) {
                    list.add(cb.equal(root.get("orgFlag"), orgFlag));
                }
                if (StrUtil.isNotBlank(helperName)) {
                    list.add(cb.equal(root.get("helperName"), helperName));
                }
                if (StrUtil.isNotBlank(email)) {
                    list.add(cb.equal(root.get("helperEmail"), email));
                }
                // 渠道过滤
                if (CollectionUtil.isNotEmpty(channel)) {
                    list.add(cb.in(root.get("helpChannel")).value(channel));
                }
                // 状态过滤
                if (CollectionUtil.isNotEmpty(status)) {
                    list.add(cb.in(root.get("status")).value(status));
                }
                if (StrUtil.isNotBlank(keyword)) {
                    list.add(cb.or(
                            cb.like(root.get("docTitle").as(String.class), "%" + keyword.trim() + "%"),
                            cb.like(root.get("helperEmail").as(String.class), "%" + keyword.trim() + "%")));
                }
                // 是否是疑难文献
                if (isDifficult != null) {
                    list.add(cb.equal(root.get("difficult").as(Boolean.class), isDifficult));
                }
                if (beginDate != null || endDate != null) {
                    Date end = endDate == null ? new Date() : endDate;
                    Date begin = beginDate == null ? DateUtil.parse("1970-01-01 00:00:00") : beginDate;
                    list.add(cb.between(root.get("gmtCreate").as(Date.class), begin, end));
                }

                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }

    }

}
