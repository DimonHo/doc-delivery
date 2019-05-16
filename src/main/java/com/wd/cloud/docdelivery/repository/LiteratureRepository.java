package com.wd.cloud.docdelivery.repository;

import cn.hutool.core.util.StrUtil;
import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2018/5/7
 * @Description:
 */
public interface LiteratureRepository extends JpaRepository<Literature, Long>, JpaSpecificationExecutor<Literature> {

    boolean existsByUnid(String unid);

    /**
     * 根据unid唯一码查询
     *
     * @param unid
     * @return
     */
    Optional<Literature> findByUnid(String unid);

    List<Literature> findByUnidIsNull();

    @Query(value = "select doc_href,doc_title from literature where unid is null group by doc_href,doc_title", nativeQuery = true)
    List<Map<String, String>> findByUnidIsNullGroupBy();

    /**
     * 根据文献标题查询文献元数据
     *
     * @param docTitle
     * @return
     */
    Literature findByDocTitle(String docTitle);


    /**
     * 根据文献标题和文献连接查询元数据
     *
     * @param docTitle
     * @param docHref
     * @return
     */
    //Literature findByDocTitleAndDocHref(String docTitle, String docHref);

    List<Literature> findByDocTitleAndDocHref(String docTitle, String docHref);

    List<Literature> findByDocHrefAndDocTitle(String docHref, String docTitle);

    List<Literature> findByDocHrefIsNullAndDocTitle(String docTitle);

    List<Literature> deleteByIdIn(List ids);

    class SpecBuilder {


        public static Specification<Literature> buildWaitResuing(Boolean reusing, String keyword) {
            return (Specification<Literature>) (root, query, cb) -> {
                List<Predicate> list = new ArrayList<Predicate>();
                if (reusing != null) {
                    list.add(cb.equal(root.get("reusing").as(Boolean.class), reusing));
                }
                if (StrUtil.isNotBlank(keyword)) {
                    list.add(cb.like(root.get("docTitle").as(String.class), "%" + keyword + "%"));
                }
                // 存在非大数据平台应助的关联文件记录
                Subquery<DocFile> subQuery = query.subquery(DocFile.class);
                Root<DocFile> subRoot = subQuery.from(DocFile.class);
                subQuery.select(subRoot).where(cb.isFalse(subRoot.get("bigDb")), cb.equal(subRoot.get("literatureId"), root));
                list.add(cb.exists(subQuery));
                Predicate[] p = new Predicate[list.size()];
                return cb.and(list.toArray(p));
            };
        }
    }
}
