package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author He Zhigang
 * @date 2018/5/22
 * @Description: 应助记录
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "give_record")
public class GiveRecord extends AbstractEntity {

    /**
     * 一个求助可能有多个应助，但只有一个应助有效，失败的应助作为应助记录存在
     */
    private Long helpRecordId;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 应助者用户名
     */
    private String giverName;

    /**
     * 应助者IP
     */
    private String giverIp;

    /**
     * 应助者类型：
     * 0：系统自动复用应助，
     * 1：管理员应助
     * 2：用户应助
     * 3：平台数据库应助
     */
    @Column(columnDefinition = "tinyint COMMENT '应助类型： 0：系统自动应助，1：管理员应助，2：用户应助，3：平台数据库应助'")
    private Integer type;

    /**
     * 0：待上传，1：待审核，2：求助第三方，3：已取消，4：已超时，5：审核不通过，6：成功，7：无结果
     */
    @Column(columnDefinition = "tinyint COMMENT '应助状态： 0：待上传，1：待审核，2：求助第三方，3：已取消，4：已超时，5：审核不通过，6：成功，7：无结果'")
    private Integer status;

    /**
     * 处理人用户名
     */
    private String handlerName;

    /**
     * 审核失败原因
     */
    private Long auditMsgId;
}
