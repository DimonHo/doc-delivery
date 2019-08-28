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

    GiveRecord deleteByHelpRecordId(Long helpRecordId);

    /**
     * 删除用户应助记录
     * @param giverName
     * @param helpRecordId
     * @param status
     * @return
     */
    Integer deleteByGiverNameAndHelpRecordIdAndStatus(String giverName,Long helpRecordId,Integer status);

    List<GiveRecord> findByHelpRecordIdAndStatusNot(Long helpRecordId, Integer auditStatus);

    /**
     * 查询待审核记录
     *
     * @param id
     * @param status
     * @return
     */
    GiveRecord findByIdAndStatus(Long id, int status);

    /**
     * 应助者的应助记录
     *
     * @param giverName
     * @param status
     * @return
     */
    List<GiveRecord> findByGiverNameAndStatus(String giverName, int status);

    /**
     * 用户应助中
     *
     * @param giverName
     * @return
     */
    @Query(value = "select * from give_record where giver_name = ?1 and status = 0", nativeQuery = true)
    GiveRecord findByGiverNameGiving(String giverName);

    List<GiveRecord> findByGiverName(String giverName);


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
     * 取消应助，删除应助记录
     *
     * @param helpRecordId
     * @param status
     * @param giverName
     * @return
     */
    void deleteByHelpRecordIdAndStatusAndGiverName(Long helpRecordId, int status, String giverName);

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
     * 已审核通过的 或 非用户应助的应助记录
     *
     * @param helpRecordId
     * @return
     */
    @Query(value = "select * FROM give_record WHERE help_record_id = ?1 AND status = 6", nativeQuery = true)
    GiveRecord findByHelpRecordIdAndStatusSuccess(Long helpRecordId);

    /**
     * 查询指定状态的记录
     *
     * @param helpRecordId
     * @param status
     * @return
     */
    Optional<List<GiveRecord>> findByHelpRecordIdAndStatus(Long helpRecordId, Integer status);

    List<GiveRecord> findByHelpRecordIdOrderByGmtModifiedDesc(Long helpRecordId);

    /**
     * 查询
     *
     * @param helpRecordId
     * @param giverName
     * @param status
     * @return
     */
    Optional<GiveRecord> findByHelpRecordIdAndGiverNameAndStatus(Long helpRecordId, String giverName, Integer status);

    List<GiveRecord> findByTypeAndStatus(Integer type, Integer status);

    /**
     * 查询超过15分钟未上传文件的用户应助
     * @return
     */
    @Query(value = "select * FROM give_record WHERE status = 0 AND (file_id IS NULL or file_id = \"\") AND 15 < TIMESTAMPDIFF(MINUTE, gmt_create, now())", nativeQuery = true)
    List<GiveRecord> findTimeOutRecord();

    /**
     * 我的应助
     *
     * @param giverName
     * @return
     */
    long countByGiverName(String giverName);

    List<GiveRecord> findByFileId(String fileId);

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
