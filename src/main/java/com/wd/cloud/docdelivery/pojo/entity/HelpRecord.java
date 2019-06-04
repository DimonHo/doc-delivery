package com.wd.cloud.docdelivery.pojo.entity;

import cn.hutool.crypto.SecureUtil;
import com.wd.cloud.commons.util.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * @author He Zhigang
 * @date 2018/5/7
 * @Description: 求助记录
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "help_record")
public class HelpRecord extends AbstractEntity {

    /**
     * 文献ID
     */
    private Long literatureId;

    /**
     * 求助的email地址
     */
    @Column(name = "helper_email")
    private String helperEmail;

    /**
     * 求助者用户名
     */
    private String helperName;

    /**
     * 求助用户的机构id
     */
    @Column(name = "org_flag")
    private String orgFlag;

    /**
     * 求助用户的机构名称
     */
    private String orgName;

    /**
     * 求助IP
     */
    @Column(name = "helper_ip")
    private String helperIp;
    /**
     * 求助渠道，1：QQ，2：SPIS，3：ZHY，4：CRS
     */
    @Column(name = "help_channel", nullable = false, columnDefinition = "tinyint default 0 COMMENT '求助渠道，1：QQ，2：SPIS，3：ZHY，4：CRS'")
    private Long helpChannel;
    /**
     * 互助状态
     * 0：待应助，
     * 1：应助中（用户已认领，15分钟内上传文件），
     * 2: 待审核（用户已应助），
     * 3：求助第三方（第三方应助），
     * 4：应助成功（审核通过或管理员应助），
     * 5：应助失败（超过15天无结果）
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 0 COMMENT '0：待应助， 1：应助中（用户已认领，15分钟内上传文件）， 2: 待审核（用户已应助）， 3：求助第三方（第三方应助）， 4：应助成功（审核通过或管理员应助）， 5：应助失败（超过15天无结果）'")
    private Integer status;

    /**
     * 是否是疑难文献
     */
    @Column(name = "is_difficult", nullable = false, columnDefinition = "bit(1) default 0 COMMENT '1：疑难文献'")
    private Boolean difficult;

    /**
     * 是否成功发送邮件
     */
    @Column(name = "is_send", nullable = false, columnDefinition = "bit(1) default 1 COMMENT '0：未发送邮件， 1：已成功发送邮件'")
    private Boolean send;

    /**
     * 是否匿名
     */
    @Column(name = "is_anonymous", nullable = false, columnDefinition = "bit(1) default 0 COMMENT '0：未匿名， 1：已匿名'")
    private Boolean anonymous;

    /**
     * 文件资源路径：http://cloud.hnlat.com/fs-server/load/123aasdf12312
     */
    private String file;

    /**
     * 最后处理人
     */
    private String handlerName;

    /**
     * 值班人员
     */
    @Column(name = "watch_name", columnDefinition = "varchar(255) COMMENT '值班人员'")
    private String watchName;


    @Column(unique = true)
    private String unid;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
    /**
     * 文献信息
     */
    @Column(name = "remark")
    private String remark;

    @PrePersist
    public void createUnid() {
        this.unid = SecureUtil.md5(this.helperEmail + this.literatureId + DateUtil.formatDate(this.gmtCreate));
    }

    @PreUpdate
    public void updateUnid() {
        this.unid = SecureUtil.md5(this.helperEmail + this.literatureId + DateUtil.formatDate(this.gmtCreate));
    }

}
