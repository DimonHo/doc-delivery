package com.wd.cloud.docdelivery.repository;

import cn.hutool.core.util.StrUtil;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2018/5/22
 * @Description:
 */
public interface GiveRecordRepository extends JpaRepository<GiveRecord, Long>, JpaSpecificationExecutor<GiveRecord> {


    /**
     * 删除用户应助记录
     * @param giverName
     * @param helpRecordId
     * @param status
     * @return
     */
    void deleteByGiverNameAndHelpRecordIdAndStatus(String giverName,Long helpRecordId,Integer status);


    /**
     * 特定状态的应助记录
     *
     * @param helpRecordId
     * @param status
     * @param giverName
     * @return
     */
    GiveRecord findByHelpRecordIdAndStatusAndGiverName(Long helpRecordId, int status, String giverName);

    /**
     * 特定应助类型的应助记录
     *
     * @param helpRecordId
     * @param status
     * @param giverType
     * @return
     */

    Optional<GiveRecord> findByHelpRecordIdAndStatusAndType(Long helpRecordId, int status, int giverType);

    /**
     * 查询指定状态的记录
     *
     * @param helpRecordId
     * @param status
     * @return
     */
    Optional<List<GiveRecord>> findByHelpRecordIdAndStatus(Long helpRecordId, Integer status);

    /**
     * 查询超过15分钟未上传文件的用户应助
     * @return
     */
    @Query(value = "select * FROM give_record WHERE status = 0 AND (file_id IS NULL or file_id = \"\") AND 15 < TIMESTAMPDIFF(MINUTE, gmt_create, now())", nativeQuery = true)
    List<GiveRecord> findTimeOutRecord();

    /**
     * 我的应助数量
     *
     * @param giverName
     * @return
     */
    long countByGiverName(String giverName);

    class SpecBuilder {
        public static Specification<GiveRecord> buildGiveRecord(List<Integer> status, String giverName) {
            return (Specification<GiveRecord>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<Predicate>();
                if (StrUtil.isNotBlank(giverName)) {
                    list.add(cb.equal(root.get("giverName"), giverName));
                }
                // 状态过滤
                if (status != null && status.size() > 0) {
                    CriteriaBuilder.In<Integer> inStatus = cb.in(root.get("status"));
                    status.forEach(inStatus::value);
                    list.add(inStatus);
                }
                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }
    }
}
