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

public interface VHelpRecordRepository extends JpaRepository<VHelpRecord, Long>, JpaSpecificationExecutor<VHelpRecord> {

    Page<VHelpRecord> findBySend(boolean isSend, Pageable pageable);

    class SpecBuilder {

        public static Specification<VHelpRecord> buildBackendList(String orgFlag, Integer status, String keyword, String beginTime, String endTime) {
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
                if (StrUtil.isNotBlank(keyword)) {
                    list.add(cb.or(
                            cb.like(root.get("docTitle").as(String.class), "%" + keyword.trim() + "%"),
                            cb.like(root.get("helperEmail").as(String.class), "%" + keyword.trim() + "%")
                            )
                    );
                }
                if (StrUtil.isNotBlank(beginTime)) {
                    list.add(cb.between(root.get("gmtCreate").as(Date.class), DateUtil.parse(beginTime), DateUtil.parse(endTime)));
                }
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
