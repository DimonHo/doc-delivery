package com.wd.cloud.docdelivery.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import io.swagger.models.auth.In;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Hu Langshi
 * @date 2019/10/24
 * @Description:
 */
public interface VHelpRawRepository extends JpaRepository<VHelpRaw, Long>, JpaSpecificationExecutor<VHelpRaw> {

//    @Query(value = "select * from v_help_raw where if(?1 != '',gmt_create = ?1,1=1) and if(?2 is not null ,is_anonymous = ?2,1=1) and if(?3 != '',help_channel = ?3,1=1) " +
//            "and if(?4 != '',helper_email = ?4,1=1) and if(?5 != '',helper_ip = ?5,1=1) and if(?6 != '',helper_name = ?6,1=1) and if(?7 != '',org_flag = ?7,1=1)" +
//           "and if(?8 != '',org_name = ?8,1=1) and if(?9 is not null,help_record_id = ?9,1=1) and if(?10 is not null,invalid = ?10,1=1)", nativeQuery = true)
//    List<VHelpRaw> findHelpRaw(Date gmtCreate, Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, String orgName, Long helpRecordId, Integer invalid);


    class SpecBuilder {

        public static Specification<VHelpRaw> findVhelpRaw(Date beginTime,Date endTime, Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, Long helpRecordId, Integer invalid) {
            return (Specification<VHelpRaw>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<>();
                if (beginTime != null || endTime != null) {
                    Date end = endTime == null ? new Date() : endTime;
                    Date begin = beginTime == null ? DateUtil.parse("2000-01-01 00:00:00") : beginTime;
                    list.add(cb.between(root.get("gmtCreate").as(Date.class), begin, end));
                }
                if (helpChannel != null){
                    list.add(cb.equal(root.get("helpChannel").as(Long.class),helpChannel));
                }
                if (anonymous != null) {
                    list.add(cb.equal(root.get("anonymous").as(Boolean.class), anonymous));
                }
                if (StrUtil.isNotBlank(helperEmail)) {
                    list.add(cb.equal(root.get("helperEmail"), helperEmail));
                }
                if (StrUtil.isNotBlank(helperIp)) {
                    list.add(cb.equal(root.get("helperEmail"), helperIp));
                }
                if (StrUtil.isNotBlank(helperName)) {
                    list.add(cb.equal(root.get("helperName"), helperName));
                }
                if (StrUtil.isNotBlank(orgFlag)) {
                    list.add(cb.equal(root.get("orgFlag"), orgFlag));
                }
                if (helpRecordId != null){
                    list.add(cb.equal(root.get("helpRecordId").as(Long.class),helpRecordId));
                }
                //是否有效
                if (invalid != null){
                    list.add(cb.equal(root.get("invalid").as(Integer.class),invalid));
                }
                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }

        public static Specification<VHelpRaw> buildVhelpRaw(String helperName,Long helpRecordId,Date beginTime,Date endTime,Boolean isDifficult,Integer isInvalid,List<Integer> status,String invalidStatus) {
            return (Specification<VHelpRaw>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<>();
                    if (StrUtil.isNotBlank(helperName)) {
                        list.add(cb.equal(root.get("helperName"), helperName));
                    }
                    if (helpRecordId != null){
                        list.add(cb.equal(root.get("helpRecordId").as(Long.class),helpRecordId));
                    }
                    if (beginTime != null || endTime != null) {
                        Date end = endTime == null ? new Date() : endTime;
                        Date begin = beginTime == null ? DateUtil.parse("2000-01-01 00:00:00") : beginTime;
                        list.add(cb.between(root.get("gmtCreate").as(Date.class), begin, end));
                    }
                    // 是否是疑难文献
                    if (isDifficult != null) {
                        list.add(cb.equal(root.get("difficult").as(Boolean.class), isDifficult));
                    }
                    //根据有效值标记查询
                    if (invalidStatus == null){
                        if (isInvalid != null) {
                            list.add(cb.equal(root.get("invalid").as(Integer.class), isInvalid));
                        }
                        if (CollectionUtil.isNotEmpty(status)) {
                            list.add(cb.in(root.get("status")).value(status));
                        }
                    }else if ("ing".equals(invalidStatus)){
                        // 进行中
                        Integer invalid = new Integer(0);
                        List<Integer> status1 = new ArrayList<>();
                        status1.add(-1);
                        status1.add(0);
                        status1.add(1);
                        status1.add(2);
                        status1.add(3);
                        list.add(cb.or(cb.equal(root.get("invalid").as(Integer.class),invalid),
                                cb.in(root.get("status")).value(status1)));
                    }else if ("fail".equals(invalidStatus)){
                        //当标记符号为fail时，求助失败
                        Integer invalid = new Integer(1);
                        List<Integer> status1 = new ArrayList<>();
                        status1.add(5);
                        list.add(cb.or(cb.equal(root.get("invalid").as(Integer.class),invalid),
                                cb.in(root.get("status")).value(status1)));
                    }else if ("success".equals(invalidStatus)){
                        //当标记符号为success时，求助成功
                        List<Integer> status1 = new ArrayList<>();
                        status1.add(4);
                        list.add(cb.in(root.get("status")).value(status1));
                    }else if ("difficult".equals(invalidStatus)){
                        //当标记符号为difficult时，为疑难文献
                        Integer invalid = new Integer(0);
                        List<Integer> status1 = new ArrayList<>();
                        status1.add(0);
                        status1.add(1);
                        status1.add(2);
                        status1.add(3);
                        Boolean diffcult = new Boolean(true);
                        list.add(cb.and((cb.or(cb.equal(root.get("invalid").as(Integer.class),invalid),
                                cb.in(root.get("status")).value(status1))),cb.equal(root.get("difficult").as(Boolean.class), diffcult)));
                    }

                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }
    }
}
