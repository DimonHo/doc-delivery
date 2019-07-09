package com.wd.cloud.docdelivery.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface VHelpRecordRepository extends JpaRepository<VHelpRecord, Long>, JpaSpecificationExecutor<VHelpRecord> {

    List<VHelpRecord> findBySend(boolean isSend);

    Page<VHelpRecord> findBySend(boolean isSend, Pageable pageable);

    /**
     * 查询用户正在应助的文献
     * @param giverName
     * @param status
     * @return
     */
    Optional<VHelpRecord> findByGiverNameAndStatus(String giverName, Integer status);

    class SpecBuilder {

        public static Specification<VHelpRecord> buildBackendList(String orgFlag, Integer status, String keyword,Integer giveType, Date beginTime, Date endTime, String watchName) {
            return (Specification<VHelpRecord>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<>();
                if (StrUtil.isNotBlank(orgFlag)) {
                    list.add(cb.equal(root.get("orgFlag"), orgFlag));
                }
                if (status != null && status != 0) {
                    //列表查询未处理
                    if (status == 1) {
                        list.add(cb.or(cb.equal(root.get("status").as(Integer.class), 0), cb.equal(root.get("status").as(Integer.class), 1), cb.equal(root.get("status").as(Integer.class), 2)));
                    } else {
                        list.add(cb.equal(root.get("status").as(Integer.class), status));
                    }
                }
                if (giveType != null){
                    list.add(cb.equal(root.get("giveType"), giveType));
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
                Date end = endTime != null ? endTime : DateUtil.offsetSecond(new Date(),-10);
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
                    Date begin = beginDate == null ? DateUtil.offsetMonth(end, -1) : beginDate;
                    list.add(cb.between(root.get("gmtCreate").as(Date.class), begin, end));
                }

                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }
    }

}
