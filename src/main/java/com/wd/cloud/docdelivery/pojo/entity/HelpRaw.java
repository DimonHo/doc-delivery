package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Hu Langshi
 * @date 2019/10/14
 * @Description:
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "help_raw")
public class HelpRaw extends AbstractEntity{

    /**
     *是否匿名求助
     */
    @Column(name = "is_anonymous", nullable = false, columnDefinition = "bit(1) default 0 COMMENT '0：未匿名， 1：已匿名'")
    private Boolean anonymous;

    /**
     * 求助渠道
     * 1：QQ
     * 2：SPIS
     * 3：ZHY
     * 4：CRS
     * 5：PAPER
     * 6：CRS_V2
     * 7:MINI
     */
    @Column(name = "help_channel", nullable = false, columnDefinition = "tinyint(4) default 0 COMMENT '求助渠道，1：QQ，2：SPIS，3：ZHY，4：CRS, 5:PAPER, 6：CRS_V2, 7:MINI'")
    private Long helpChannel;

    /**
     * 求助者邮箱
     */
    @Column(name = "helper_email")
    private String helperEmail;

    /**
     * 求助者IP
     */
    @Column(name = "helper_ip")
    private String helperIp;

    /**
     * 求助者用户名
     */
    @Column(name = "helper_name")
    private String helperName;

    /**
     * 求助者学校ID
     */
    @Column(name = "org_flag")
    private String orgFlag;

    /**
     * 求助者学校名称
     */
    @Column(name = "org_name")
    private String orgName;

    /**
     * 求助的信息
     */
    @Column(name = "info")
    private String info;

    /**
     * 求助记录的ID
     */
    @Column(name = "help_record_id", nullable = false, columnDefinition = "bigint(20) default 0")
    private Long helpRecordId;

    /**
     * 是否有效
     * 0:待处理
     * 1：无效
     * 2：有效
     */
    @Column(name = "is_invalid", nullable = false, columnDefinition = "tinyint(1) NOT NULL COMMENT '是否有效：0待处理，1：无效；2：有效'")
    private Integer invalid;

}
