package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author He Zhigang
 * @date 2019/1/7
 * @Description: 渠道配置表
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "channel")
public class Channel extends AbstractEntity {

    private String name;
    private String url;
    private String template;
    /**
     * 下载链接有效期
     */
    @Column(columnDefinition = "bigint(11) default 1296000000 COMMENT '全文下载过期时间'")
    private Long exp;
    /**
     * 密送邮箱
     */
    private String bccs;
}
