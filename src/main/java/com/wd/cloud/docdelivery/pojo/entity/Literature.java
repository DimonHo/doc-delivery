package com.wd.cloud.docdelivery.pojo.entity;

import cn.hutool.crypto.SecureUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author He Zhigang
 * @date 2018/5/3
 * @Description: 文献元数据
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "literature", uniqueConstraints = {@UniqueConstraint(columnNames = {"unid"})})
public class Literature extends AbstractEntity {

    /**
     * 文献的链接地址
     */
    @Column(name = "doc_href", length = 1000)
    private String docHref;
    /**
     * 文献标题
     */
    @NotNull
    @Column(name = "doc_title", length = 1000)
    private String docTitle;

    @Column(name = "unid")
    private String unid;

    private String journal;
    /**
     * doi
     */
    private String doi;
    /**
     * 摘要
     */
    private String summary;

    private String issn;

    private String issue;

    private String volume;

    /**
     * 发表年份
     */
    private String year;

    /**
     * 文献作者
     */
    private String author;

    /**
     * 复用
     */
    @Column(name = "is_reusing", nullable = false, columnDefinition = "bit(1) default 0 COMMENT '0:未复用，1：已复用'")
    private Boolean reusing;
    /**
     * 最后处理人
     */
    private String lastHandlerName;

    @PrePersist
    public void createUnid() {
        this.unid = SecureUtil.md5(this.docTitle + this.docHref);
    }

    @PreUpdate
    public void updateUnid() {
        this.unid = SecureUtil.md5(this.docTitle + this.docHref);
    }
}
