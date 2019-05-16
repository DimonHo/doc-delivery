package com.wd.cloud.docdelivery.repository;

import com.wd.cloud.docdelivery.pojo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Wu Qilong
 * @date 2018/12/22
 */
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Permission findByOrgFlagAndLevel(String orgFlag, Integer level);

    Permission findByOrgFlagIsNullAndLevel(Integer level);

    @Query(value = "select * from permission where org_flag = ?1 and level = ?2", nativeQuery = true)
    Permission getOrgFlagAndLevel(String orgFlag, int level);

    Permission getByLevelAndOrgFlag(int level, String orgFlag);
}
