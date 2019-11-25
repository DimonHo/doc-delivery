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

    /**
     * 查询指定机构
     * @param orgFlag 机构
     * @param level 等级
     * @param channel 渠道
     * @return
     */
    Permission findByOrgFlagAndLevelAndChannel(String orgFlag, Integer level,Long channel);

    /**
     * 查询默认
     * @param level 等级
     * @param channel 渠道
     * @return
     */
    Permission findByOrgFlagIsNullAndLevelAndChannel(Integer level,Long channel);

}
