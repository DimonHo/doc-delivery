package com.wd.cloud.docdelivery.repository;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.wd.cloud.commons.util.DateUtil;
import com.wd.cloud.docdelivery.pojo.dto.TjDTO;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author He Zhigang
 * @date 2019/3/11
 * @Description:
 */
@Component
public class SpecBuilder {

    @PersistenceContext
    EntityManager entityManager;

    public TjDTO tj(String orgFlag, String helperEmail, String helperName, String beginDate, String endDate, String format) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TjDTO> query = cb.createQuery(TjDTO.class);
        Root<VHelpRecord> root = query.from(VHelpRecord.class);
        Path<String> orgFlagPath = root.get("orgFlag");
        Path<String> helperEmailPath = root.get("helperEmail");
        Path<String> helperNamePath = root.get("helperName");
        Path<Integer> statusPath = root.get("status");
        Path<Date> gmtCreatePath = root.get("gmt_create");


        //拼接where条件
        List<Predicate> predicateList = new ArrayList<>();
        if (orgFlag != null) {
            predicateList.add(cb.equal(orgFlagPath, orgFlag));
            query.multiselect(orgFlagPath).groupBy(orgFlagPath);
        }
        if (StrUtil.isNotBlank(helperEmail)) {
            predicateList.add(cb.equal(helperEmailPath, helperEmail));
            query.multiselect(helperEmailPath).groupBy(helperEmailPath);
        }
        if (StrUtil.isNotBlank(helperName)) {
            predicateList.add(cb.equal(helperNamePath, helperName));
            query.multiselect(helperNamePath).groupBy(helperNamePath);
        }
        if (StrUtil.isNotBlank(format)) {
            Selection<String> tjDate = cb.function("DATE_FORMAT", String.class, gmtCreatePath, cb.literal(format)).alias("tjDate");
        }
        if (StrUtil.isNotBlank(beginDate)) {
            endDate = StrUtil.isBlank(endDate) ? DateUtil.now() : endDate;
            predicateList.add(cb.between(cb.function("DATE_FORMAT", Date.class, gmtCreatePath, cb.literal(format)), DateUtil.parse(beginDate), DateUtil.parse(endDate)));
//            query.multiselect(tjDate).groupBy(tjDate);
        }


        //加上where条件
        query.where(ArrayUtil.toArray(predicateList, Predicate.class));
        // 总数
        Selection<Long> sumCount = cb.count(root).alias("sumCount");
        // 成功数
        Selection<Long> successCount = cb.sum(cb.<Long>selectCase().when(cb.equal(statusPath, 4), 1L).otherwise(0L)).alias("successCount");
        query.multiselect(sumCount, successCount);

        TypedQuery<TjDTO> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }
}
