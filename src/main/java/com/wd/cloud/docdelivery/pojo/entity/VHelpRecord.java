package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author He Zhigang
 * @date 2019/1/5
 * @Description: v_help_record视图类
 */
@Data
@Accessors(chain = true)
@Immutable
@Entity
@Table(name = "v_help_record")
public class VHelpRecord {

    @Id
    private Long id;

    private Date gmtCreate;

    private Date gmtModified;

    private Long literatureId;

    private String docTitle;

    private String docHref;

    private String author;

    private String year;

    private String doi;

    private String summary;

    private String helperEmail;

    private String helperName;

    private String watchName;

    private String orgFlag;

    private String orgName;

    private String helperIp;

    private Long helpChannel;

    private String channelName;

    private String channelUrl;

    private String channelTemplate;

    private String bccs;

    private Long exp;

    private Integer status;

    private Integer giveType;

    private String giverName;

    @Column(name = "is_send")
    private Boolean send;

    @Column(name = "is_difficult")
    private Boolean difficult;

    @Column(name = "is_anonymous")
    private Boolean anonymous;

    private String unid;

    private String remark;
}
