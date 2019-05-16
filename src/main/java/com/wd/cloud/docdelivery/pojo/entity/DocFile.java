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
 * @date 2018/5/27
 * @Description:
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "doc_file",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"literature_id", "file_id"})})
public class DocFile extends AbstractEntity {

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "literature_id")
    private Long literatureId;

    /**
     * 仓库类型，1：fs-server,2：大数据平台全文仓库
     */
    @Column(name = "is_big_db", nullable = false, columnDefinition = "bit(1) default 0 COMMENT '0:文件上传，1：数据平台'")
    private Boolean bigDb;
    /**
     * 复用
     */
    @Column(name = "is_reusing", nullable = false, columnDefinition = "bit(1) default 0 COMMENT '0:未复用，1：已复用'")
    private Boolean reusing;

    private String handlerName;

}
