package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author He Zhigang
 * @date 2018/12/21
 * @Description:
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "permission", uniqueConstraints = {@UniqueConstraint(columnNames = {"org_flag", "level"})})
public class Permission extends AbstractEntity {

    @Column(name = "org_flag")
    private String orgFlag;

    private String orgName;

    private Integer channel;
    /**
     * 校内：1，登陆：2，验证：4，最后相加得到权限
     */
    @Column(name = "level")
    private Integer level;

    /**
     * 每天求助上限 ，null则表示无上限
     */
    private Long todayTotal;

    /**
     * 总求助上限，null则表示无上限
     */
    private Long total;
}
